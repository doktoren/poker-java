package poker.statistics;

import poker.representation.ParseException;

public class BuildSavedGame {

	GameSetup gameSetup;
	
	// First 2*numPlayers, then flop, turn, river
	// 52 represents unknown cards
	private int[] cards;
	
	// First players.length are bets+antes
	// A bet of -1 is a fold. 0 is check or no action possible (all in).
	private int bets[];
	
	public boolean[] activePlayers; // Initially true
	public int[] investments; // How much has each player in total in the pot?
	
	public int deadMoney;
	
	private int numBets;
	private int numBoardCards;
	
	
	public void debug() {
		for (int i=0; i<activePlayers.length; i++)
			System.out.println(gameSetup.players[i]+" "+activePlayers[i]+" "+gameSetup.startChips[i]+" "+investments[i]);
	}
	
	public boolean isActivePlayer(int playerIndex) {
		return activePlayers[playerIndex]  &&  gameSetup.startChips[playerIndex] > investments[playerIndex];
	}
	
	public BuildSavedGame(GameSetup gameSetup) {
		this.gameSetup = gameSetup;
		cards = new int[2*gameSetup.numPlayers + 5];
		bets = new int[16];
		activePlayers = new boolean[gameSetup.numPlayers];
		for (int i=0; i<activePlayers.length; i++)
			activePlayers[i] = true;
		investments = new int[gameSetup.numPlayers];
		for (int i=0; i<investments.length; i++)
			investments[i] = 0;
	}
	
	public void makeBet(int playerIndex, int bet) throws ParseException {
		if (numBets+1 == bets.length) {
			int[] tmp = new int[2*bets.length];
			for (int i=0; i<bets.length; i++)
				tmp[i] = bets[i];
			bets = tmp;
		}
		bets[numBets++] = bet;
		if (bet != -1)
			investments[playerIndex] += bet;
		ParseException.Assert(investments[playerIndex] <= gameSetup.startChips[playerIndex]);
	}
	
	public void setPlayerCards(int playerIndex, int card1, int card2) {
		if (playerIndex<0  ||  gameSetup.numPlayers<=playerIndex)
			throw new RuntimeException("Illegal player index: "+playerIndex);
		cards[2*playerIndex] = card1;
		cards[2*playerIndex + 1] = card2;
	}
	
	public void setFlop(int card1, int card2, int card3) {
		if (numBoardCards != 0)
			throw new RuntimeException("numBoardCards = "+numBoardCards);
		cards[2*gameSetup.numPlayers] = card1;
		cards[2*gameSetup.numPlayers + 1] = card2;
		cards[2*gameSetup.numPlayers + 2] = card3;
		numBoardCards = 3;
	}
	
	public void setTurn(int card4) {
		if (numBoardCards != 3)
			throw new RuntimeException("numBoardCards = "+numBoardCards);
		cards[2*gameSetup.numPlayers + 3] = card4;
		numBoardCards = 4;
	}
	
	public void setRiver(int card5) {
		if (numBoardCards != 4)
			throw new RuntimeException("numBoardCards = "+numBoardCards);
		cards[2*gameSetup.numPlayers + 4] = card5;
		numBoardCards = 5;
	}
	
	public SavedGame trim() {
		int[] _cards = new int[2*gameSetup.numPlayers + numBoardCards];
		for (int i=0; i<_cards.length; i++)
			_cards[i] = cards[i];

		int[] _bets = new int[numBets];
		for (int i=0; i<_bets.length; i++)
			_bets[i] = bets[i];
		
		return new SavedGame(gameSetup, _cards, _bets, deadMoney);
	}

}
