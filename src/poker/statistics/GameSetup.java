package poker.statistics;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import poker.io.ReadWriteArray;
import poker.io.ReadWriteHashMap;
import poker.io.ReadWriteIntArray;
import poker.io.ReadWriteObject;
import poker.representation.ParseException;
import poker.util.ParseMisc;


public class GameSetup {
	
	public static final GameSetup.RW rw = new GameSetup.RW();
	
	final TableSetup tableSetup;

	//String time = "2006:06:08 17:52:32";  - Needed for merging hands into correct sets.
	//String gameNumber = 7012343
	
	public static final String TIME = "time";
	public static final String GAME_NUMBER = "gameNumber";
	
	public final HashMap<String, String> optional = new HashMap<String, String>();
	
	public int numPlayers; // derived from players.length
	
	// players[players.length-1] is dealer
	public String[] players;
	public int[] startChips;
	
	public void toHTML(StringBuffer html) throws ParseException {
		tableSetup.toHTML(html);
		html.append("Game setup:<UL>\n");
		Iterator<Entry<String, String>> it = optional.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			html.append("<LI>"+entry.getKey()+" = "+entry.getValue()+"</LI>\n");
		}
		html.append("</UL>\n");
		html.append("The "+numPlayers+" players are:<UL>\n");
		for (int i=0; i<numPlayers; i++)
			html.append("<LI>"+players[i]+" has "+ParseMisc.chipCountToString(startChips[i])+"</LI>\n");
		html.append("</UL>\n");
	}
	
	public String requireOptional(String optionalName) throws ParseException {
		String value = (String)optional.get(optionalName);
		if (value == null)
			throw new ParseException("The optional "+optionalName+" is not set.");
		return value;
	}
	
	public void setNumPlayers(int numPlayers) {
		this.numPlayers = numPlayers;
		players = new String[numPlayers];
		startChips = new int[numPlayers];
	}
	
	public int getPlayerIndex(String player) throws ParseException {
		for (int i=0; i<players.length; i++)
			if (player.equals(players[i]))
				return i;
		throw new ParseException("Player "+player+" not found");
	}
	
	public GameSetup(TableSetup tableSetup) {
		this.tableSetup = tableSetup;
	}
	
	public static class RW extends ReadWriteObject {

		private final TableSetup reuseThis;
		
		public RW() {
			reuseThis = null;
		}
		
		public RW(TableSetup ts) {
			reuseThis = ts;
		}
		
		@Override
		public Class getObjectClass() {
			return GameSetup.class;
		}
		
		@Override
		public int size(Object obj) {
			GameSetup gs = (GameSetup)obj;
			return (reuseThis==null ? TableSetup.rw.size(gs.tableSetup) : 0) + ReadWriteHashMap.rw.size(gs.optional) +
				ReadWriteArray.stringRW.size(gs.players) + ReadWriteIntArray.size(gs.startChips);
		}

		@Override
		public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
			GameSetup gs = (GameSetup)obj;
			if (reuseThis==null)
				TableSetup.rw.write(bb, gs.tableSetup);
			ReadWriteHashMap.rw.write(bb, gs.optional);
			ReadWriteArray.stringRW.write(bb, gs.players);
			ReadWriteIntArray.write(bb, gs.startChips);
		}

		@Override
		public Object read(ByteBuffer bb, int maxSize) throws ParseException {
			GameSetup result = new GameSetup(reuseThis!=null ? reuseThis : (TableSetup)TableSetup.rw.read(bb));
			ReadWriteHashMap.rw.read(bb, result.optional, 1000);
			result.players = (String[])ReadWriteArray.stringRW.read(bb, 1000);
			result.startChips = ReadWriteIntArray.read(bb, 44);
			result.numPlayers = result.players.length;
			return result;
		}

	}
}
