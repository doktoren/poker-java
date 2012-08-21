package poker.statistics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import poker.representation.ParseException;
import poker.representation.PokerMisc;
import poker.util.ParseMisc;
import poker.util.StringMisc;


/**
 * Starts a thread which continuously reads from READ_DIR and
 * generates new data for WRITE_DIR.
 * 
 * @author Jesper Kristensen
 */
/**
 * @author Jesper Kristensen
 */
public class HandGrabberConverter extends Thread {

	public static final int MAX_FILE_SIZE = 16*1024;
	
	private int gamesRead;
	
	public final File readFrom;
	public final File rejected;
	public final Database database;
	
	public static void main(String[] args) {
		Database db = new Database("C:\\poker database\\");
		db.startServer();
		
		Thread t = new HandGrabberConverter("C:\\poker statistics\\", db);
		t.start();
	}
	
	public HandGrabberConverter(String readFrom, Database database) {
		this.readFrom = new File(readFrom);
		this.rejected = new File(readFrom+"rejected");
		rejected.mkdirs();
		this.database = database;
	}
	
	public void run() {
		try {
			if (true) { database.load(); return; }
			
			recurseDirectory(readFrom, false);
			database.save();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			System.out.println("ARGH!!!!!!!!!!!!!!! "+t);
			t.printStackTrace();
			System.out.println("ARGH!!!!!!!!!!!!!!! "+t);
		}
	}
	
	private void recurseDirectory(File directory, boolean delete) throws ParseException, IOException {
		if (directory.equals(rejected))
			return;
		if (!directory.isDirectory())
			throw new RuntimeException(""+directory+" is not a directory");
		File[] unprocessedFiles = directory.listFiles();
		
		// Assures that "The games must be added in the order that they were played"
		Arrays.sort(unprocessedFiles);
		
		for (int i=0; i<unprocessedFiles.length; i++)
			if (unprocessedFiles[i].isDirectory()) {
				recurseDirectory(unprocessedFiles[i], delete);
			} else {
				processFile(unprocessedFiles[i], delete);
			}
	}
	
	private void processFile(File file, boolean delete) throws ParseException, IOException {

		if (!file.toString().endsWith(".txt"))
			return;
		
		//if (gamesRead > 100) return;
		
		byte[] buf = new byte[MAX_FILE_SIZE];
		int bytesRead = 0;
		FileInputStream fis = new FileInputStream(file);
		try { 
			do {
				int n = fis.read(buf, bytesRead, MAX_FILE_SIZE-bytesRead);
				if (n == -1)
					break;
				if ((bytesRead += n) == MAX_FILE_SIZE)
					throw new RuntimeException("MAX_FILE_SIZE is set too low.");
			} while (true);
		} finally {
			fis.close();
		}
			
		
		String stringRep = new String(buf, 0, bytesRead);
		SavedGame game;
		try {
			++gamesRead;
			System.out.println(""+gamesRead);
			//System.out.println("Trying to process game "+(++gamesRead)+": "+file);
			game = processGame(new MyStringTokenizer(stringRep));
			//System.out.println("Processed game "+file+" successfully!");
			database.insertSavedGame(game);
		} catch (ParseException e) {
			System.out.println("\nParse Exception occured in this game:");
			System.out.println(stringRep);
			e.printStackTrace();
			System.out.println("\nMoving file to rejected folder.\n");
			
			file.renameTo(new File(rejected, file.getName()));
		}
	}
	
	private SavedGame processGame(MyStringTokenizer st) throws ParseException {
		TableSetup tableSetup = new TableSetup();
		tableSetup.rakeFunction = 1;
		tableSetup.optional.put(TableSetup.POKER_ROOM, PokerSites.PACIFIC_POKER);
		
		GameSetup gameSetup = new GameSetup(tableSetup);
		
		//#Game No : 178584061
		//***** Pacific Hand History for Game 178584061 *****
		
		st.requireSpecificTokens(new String[]{"#Game", "No", ":"});
		
		gameSetup.optional.put(GameSetup.GAME_NUMBER, ""+st.readInt());
		
		st.requireSpecificTokens(new String[]{"*****", "Pacific", "Hand", "History", "for", "Game",
				gameSetup.optional.get(GameSetup.GAME_NUMBER), "*****"});
		
		{ // Parse blinds
			String blinds = st.nextToken();
			int n = blinds.lastIndexOf('$');
			if (n+1 == blinds.length())
				throw new ParseException("No bb after last $");
			tableSetup.bigBlind = ParseMisc.readChipCount(blinds.substring(n+1));
			n = blinds.indexOf('/');
			if (n<2)
				throw new ParseException("Illegal index of '/': "+n);
			int sb = ParseMisc.readChipCount(blinds.substring(1, n));
			String tmp = "$"+ParseMisc.chipCountToString(sb)+"/$"+ParseMisc.chipCountToString(tableSetup.bigBlind);
			ParseException.Assert(blinds.equals(tmp), blinds+" differ from "+tmp);
			tableSetup.optional.put(TableSetup.VARIANT, blinds+" Blinds No Limit Hold'em");
		}
		st.requireSpecificTokens(new String[]{"Blinds", "No", "Limit", "Hold'em", "-", "***"});
		
		{ // Parse time
			int month = st.readInt();
			ParseException.Assert(1<=month && month<=12);
			int day = st.readInt();
			ParseException.Assert(1<=day && day<=31);
			String time = st.nextToken();
			ParseException.Assert(time.length()==8  &&  time.charAt(2)==':'  &&  time.charAt(5)==':');
			int year = st.readInt();
			gameSetup.optional.put(GameSetup.TIME, ""+year+"."+StringMisc.rightJustify(""+month, 2, '0')+"."+
					StringMisc.rightJustify(""+day, 2, '0')+" "+time);
		}
		
		String tableOrTournament = st.nextToken();
		ParseException.Assert("Table".equals(tableOrTournament)  ||  "Tournament".equals(tableOrTournament));
		if ("Table".equals(tableOrTournament)) {
			String tableName = st.parseUntil("Seat");
			tableSetup.optional.put(TableSetup.TABLE_NAME, tableName);
		} else {
			// TODO: probably read something else than table name
			String tableName = st.parseUntil("Seat");
			tableName = tableName.replace("#", "");
			tableSetup.optional.put(TableSetup.TABLE_NAME, tableName);
		}
		
		{// Read players and their chip counts.
			int buttonPos = st.readInt();
			st.requireSpecificTokens(new String[]{"is", "the", "button"});
			st.requireSpecificTokens(new String[]{"Total", "number", "of", "players", ":"});
			gameSetup.setNumPlayers(st.readInt());
			
			int buttonIndex = -1;
			String[] players = new String[gameSetup.numPlayers];
			int[] startChips = new int[gameSetup.numPlayers];
			for (int i=0; i<gameSetup.numPlayers; i++) {
				st.requireSpecificToken("Seat");
				String tmp = st.nextToken();
				if (ParseMisc.parseInt(tmp.substring(0, tmp.length()-1)) == buttonPos)
					buttonIndex = i;
				players[i] = st.nextToken();
				st.requireSpecificToken("(");
				startChips[i] = ParseMisc.readChipCount(st.nextToken());
				st.requireSpecificToken(")");
			}
			if (buttonIndex == -1)
				throw new ParseException("buttonIndex have not been initialized");
			
			// Change so that dealer gets last index
			int moveButton = (gameSetup.numPlayers-1) - buttonIndex;
			for (int i=0; i<gameSetup.numPlayers; i++) {
				gameSetup.players[(i+moveButton)%gameSetup.numPlayers] = players[i];
				gameSetup.startChips[(i+moveButton)%gameSetup.numPlayers] = startChips[i];
			}
		}
		
		// Finished read tableSetup and gameSetup.
		
		BuildSavedGame game = new BuildSavedGame(gameSetup);
		
		
		try {
			{// Reading posted blinds (make pre betting)
				int[] blinds = new int[gameSetup.numPlayers];
				
				{ // Read small blind
					String sbPlayer = st.nextToken();
					ParseException.Assert(gameSetup.getPlayerIndex(sbPlayer) == (gameSetup.numPlayers==2 ? 1 : 0));
					st.requireSpecificTokens(new String[]{"posts", "small", "blind"});
					String sb = st.nextToken();
					blinds[gameSetup.getPlayerIndex(sbPlayer)] = ParseMisc.readChipCount(sb.substring(2, sb.length()-2));
				}
				
				{ // Read big blind
					String bbPlayer = st.nextToken();
					ParseException.Assert(gameSetup.getPlayerIndex(bbPlayer) == (gameSetup.numPlayers==2 ? 0 : 1));
					st.requireSpecificTokens(new String[]{"posts", "big", "blind"});
					String bb = st.nextToken();
					blinds[gameSetup.getPlayerIndex(bbPlayer)] = ParseMisc.readChipCount(bb.substring(2, bb.length()-2));
				}
				
				{ // Read any extra blinds being posted
					String player;
					while (!"**".equals(player = st.nextToken())) {
						// This will probably have to be changed.
						if (st.matchesSpecificTokens(new String[]{"posts", "big", "blind"})) {
							String blind = st.nextToken();
							blinds[gameSetup.getPlayerIndex(player)] = ParseMisc.readChipCount(blind.substring(2, blind.length()-2));
						} else {
							st.requireSpecificTokens(new String[]{"posts", "dead", "blind"});
							String dead = st.nextToken();
							int deadChips = ParseMisc.readChipCount(dead.substring(2, dead.length()-2));
							//System.err.println("Ignoring: "+player+" post dead blind $"+ParseMisc.chipCountToString(deadChips)+" in game "+gamesRead);
							
							game.deadMoney += deadChips;
							gameSetup.startChips[gameSetup.getPlayerIndex(player)] -= deadChips;
						}
					}
					st.pushBack(player);
				}
				
				for (int i=0; i<gameSetup.numPlayers; i++)
					game.makeBet(i, blinds[i]);
			}
			
			
			st.requireSpecificTokens(new String[]{"**", "Dealing", "down", "cards", "**"});
			
			{// Initialize cards dealt
				for (int i=0; i<gameSetup.numPlayers; i++)
					game.setPlayerCards(i, 52, 52);
				
				String next = st.nextToken();
				if ("Dealt".equals(next)) {
					//Dealt to doktoren [ 2d 9h ]
					st.requireSpecificToken("to");
					String dealtTo = st.nextToken();
					int playerIndex = gameSetup.getPlayerIndex(dealtTo);
					st.requireSpecificToken("[");
					int card1 = getFromShortName(st.nextToken());
					int card2 = getFromShortName(st.nextToken());
					game.setPlayerCards(playerIndex, card1, card2);
					st.requireSpecificToken("]");
				} else {
					st.pushBack(next);
				}
			}
			
			//game.debug();
			
			// Betting rounds
			String dealingOrSummary;
			parseBettingRound(game, st, game.gameSetup.numPlayers==2 ? 1 : 2);
			st.requireSpecificToken("**");
			if ((dealingOrSummary = st.nextToken()).equals("Dealing")) {
				st.requireSpecificTokens(new String[]{"Flop", "**", "["});
				String next = st.nextToken();
				int card1 = getFromShortName(next.substring(0, next.length()-1));
				next = st.nextToken();
				int card2 = getFromShortName(next.substring(0, next.length()-1));
				int card3 = getFromShortName(st.nextToken());
				game.setFlop(card1, card2, card3);
				st.requireSpecificToken("]");
				
				parseBettingRound(game, st, 0);
				st.requireSpecificToken("**");
				if ((dealingOrSummary = st.nextToken()).equals("Dealing")) {
					st.requireSpecificTokens(new String[]{"Turn", "**", "["});
					game.setTurn(getFromShortName(st.nextToken()));
					st.requireSpecificToken("]");
					
					parseBettingRound(game, st, 0);
					st.requireSpecificToken("**");
					if ((dealingOrSummary = st.nextToken()).equals("Dealing")) {
						st.requireSpecificTokens(new String[]{"River", "**", "["});
						game.setRiver(getFromShortName(st.nextToken()));
						st.requireSpecificToken("]");
						
						parseBettingRound(game, st, 0);
						st.requireSpecificToken("**");
						dealingOrSummary = st.nextToken();
					}
				}
			}
			
			ParseException.Assert("Summary".equals(dealingOrSummary));
			st.requireSpecificToken("**");
			while (st.hasMoreTokens()) {
				int playerIndex = gameSetup.getPlayerIndex(st.nextToken());
				String next = st.nextToken();
				if ("shows".equals(next)) {
					st.requireSpecificToken("[");
					int card1 = getFromShortName(st.nextToken());
					int card2 = getFromShortName(st.nextToken());
					game.setPlayerCards(playerIndex, card1, card2);
					st.requireSpecificToken("].");
				} else if ("mucks.".equals(next)) {
					// ignore.
				} else {
					ParseException.Assert("collected".equals(next));
					String tmp = st.nextToken();
					ParseException.Assert(tmp.startsWith("[$")  &&  tmp.endsWith("]."));
				}
			}
			
			// TODO: verify collected
			
			return game.trim();
		
		} catch (ParseException e) {
			game.debug();
			throw e;
		}
	}
	
	private void parseBettingRound(BuildSavedGame game, MyStringTokenizer st, int betPos) throws ParseException {
		String player = st.nextToken();
		while (!"**".equals(player)) {
			while (!game.isActivePlayer(betPos))
				betPos = (betPos+1)%game.gameSetup.numPlayers;
			
			// Do we agree on which turn it is?
			ParseException.Assert(player.equals(game.gameSetup.players[betPos]),
					player+" differs from "+game.gameSetup.players[betPos]);
			
			String action = st.nextToken();
			if ("folds.".equals(action)) {
				game.makeBet(betPos, -1);
				game.activePlayers[betPos] = false;
			} else if ("checks.".equals(action)) {
				game.makeBet(betPos, 0);
			} else if ("calls".equals(action)  ||  "bets".equals(action)  ||  "raises".equals(action)) {
				String amount = st.nextToken();
				ParseException.Assert(amount.startsWith("[$")  &&  amount.endsWith("]."));
				game.makeBet(betPos, ParseMisc.readChipCount(amount.substring(2, amount.length()-2)));
			} else {
				throw new ParseException("Illegal action: "+action);
			}
			
			player = st.nextToken();
			betPos = (betPos+1)%game.gameSetup.numPlayers;
		}
		st.pushBack(player);
	}
	
	/**
	 * There is a slight difference in the format of short names.
	 */
	public int getFromShortName(String shortName) throws ParseException {
		if (shortName.startsWith("10"))
			shortName = "T"+shortName.substring(2);
		return PokerMisc.getFromShortName(shortName);
	}
	
}
