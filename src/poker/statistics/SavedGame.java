package poker.statistics;


import java.nio.ByteBuffer;

import poker.io.ReadWriteIntArray;
import poker.io.ReadWriteObject;
import poker.representation.ParseException;
import poker.representation.PokerMisc;
import poker.util.ParseMisc;


/**
 * Money is counted in cents.
 * 
 * @author Jesper Kristensen
 */
public class SavedGame {
	
	public static final SavedGame.RW rw = new SavedGame.RW();
	
	GameSetup gameSetup;
	
	final int deadMoney;
	
	// First 2*numPlayers, then flop, turn, river
	// 52 represents unknown cards
	final int[] cards;
	
	// First players.length are bets+antes (starting from the player after bb)
	// A bet of -1 is a fold. 0 is check or no action possible (all in).
	final int bets[];
	
	public SavedGame(GameSetup gameSetup, int[] cards, int[] bets, int deadMoney) {
		this.gameSetup = gameSetup;
		this.cards = cards;
		this.bets = bets;
		this.deadMoney = deadMoney;
	}
	
	public void toHTML(StringBuffer html) throws ParseException {
		gameSetup.toHTML(html);
		if (deadMoney != 0)
			html.append("Dead money = "+ParseMisc.chipCountToString(deadMoney)+"<BR>\n");
		
		html.append("Cards:<UL>\n");
		for (int i=0; i<gameSetup.numPlayers; i++)
			html.append("<LI>"+gameSetup.players[i]+" : "+
					PokerMisc.getShortName(cards[2*i])+" "+PokerMisc.getShortName(cards[2*i+1])+"</LI>\n");
		int n = 2*gameSetup.numPlayers; 
		if (n+3 <= cards.length)
			html.append("<LI>Flop: "+PokerMisc.getShortName(cards[n])+" "+PokerMisc.getShortName(cards[n+1])+" "+PokerMisc.getShortName(cards[n+2])+"</LI>\n");
		if (n+4 <= cards.length)
			html.append("<LI>Turn: "+PokerMisc.getShortName(cards[n+3])+"</LI>\n");
		if (n+5 <= cards.length)
			html.append("<LI>River: "+PokerMisc.getShortName(cards[n+4])+"</LI>\n");
		html.append("</UL>\n");
		
		html.append("Bets:");
		for (int i=0; i<bets.length; i++)
			html.append(" "+ParseMisc.chipCountToString(bets[i]));
		html.append("<BR>\n");
	}
	
	public static class RW extends ReadWriteObject {
		
		private final GameSetup.RW gsRW;
		
		public RW() {
			gsRW = GameSetup.rw;
		}
		
		public RW(TableSetup ts) {
			gsRW = new GameSetup.RW(ts);
		}
		
		@Override
		public Class getObjectClass() {
			return SavedGame.class;
		}
		
		@Override
		public int size(Object obj) {
			SavedGame sg = (SavedGame)obj;
			return gsRW.size(sg.gameSetup) + 4 + ReadWriteIntArray.size(sg.cards) + ReadWriteIntArray.size(sg.bets);
		}
		
		@Override
		public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
			SavedGame sg = (SavedGame)obj;
			gsRW.write(bb, sg.gameSetup);
			bb.putInt(sg.deadMoney);
			ReadWriteIntArray.write(bb, sg.cards);
			ReadWriteIntArray.write(bb, sg.bets);
		}
		
		@Override
		public Object read(ByteBuffer bb, int maxSize) throws ParseException {
			GameSetup gameSetup = (GameSetup)gsRW.read(bb, 2000);
			int deadMoney = bb.getInt();
			int[] cards = ReadWriteIntArray.read(bb, 200);
			int[] bets = ReadWriteIntArray.read(bb, 10000);
			return new SavedGame(gameSetup, cards, bets, deadMoney);
		}
	}
}


/**
 * Ignored properties:
 * 		1) Which players are sitting at which seat numbers.
 * 		2) Who are showing their cards at the end.
 * 		3) Some head up games may have the blinds switched.
 */
