package poker.representation;

import java.util.Arrays;
import java.util.StringTokenizer;

public class PokerMisc {
	
	public static final char[] RANK_CHAR = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
	public static final char[] SUIT_CHAR = {'h', 'c', 'd', 's'};
	
	public static final String[] RANK_NAME = {"2","3","4","5","6","7","8","9","10","jack","queen","king","ace"};
	public static final String[] SUIT_NAME = {"hearts", "clubs", "diamonds", "spades"};
	
	public static final int getRank(int card) {
		return card>>2;
	}
	
	public static final int getSuit(int card) {
		return card&3;
	}
	
	public static final int getCard(int rank, int suit) {
		return (rank << 2) | suit;
	}
	
	public static final String getShortName(int card) {
		if (card==52)
			return "??";
		return new String(new char[]{RANK_CHAR[getRank(card)], SUIT_CHAR[getSuit(card)]});
	}
	
	/**
	 * Sorts the cards in ascending order.
	 */
	public static final void sort(int[] cards) {
		Arrays.sort(cards);
	}
	
	/**
	 * Returns a space separated String of short names for the cards.
	 */
	public static final String getShortNames(int[] cards) {
		if (cards.length == 0)
			return "";
		StringBuffer sb = new StringBuffer(getShortName(cards[0]));
		for (int i=1; i<cards.length; i++)
			sb.append(" "+getShortName(cards[i]));
		return sb.toString();
	}
	
	public static final String getShortNamePair(int card1, int card2) {
		return getShortName(card1)+'-'+getShortName(card2);
	}
	
	public static final String getShortNamePair(int cardPair) {
		return getShortName(decodeCard1(cardPair))+'-'+getShortName(decodeCard2(cardPair));
	}
	
	public static final String getLongName(int card) {
		if (card==52)
			return "unknown card";
		return RANK_NAME[getRank(card)] + " of " + SUIT_NAME[getSuit(card)];
	}

	public static final int getFromShortName(String shortName) throws ParseException {
		if (shortName.length() != 2)
			throw new ParseException("length must be 2: "+shortName);
		
		if ("??".equals(shortName))
			return 52;
		
		char rankChar = shortName.charAt(0);
		int rank = 13;
		for (int i=0; i<13; i++)
			if (rankChar == RANK_CHAR[i]) {
				rank = i;
				break;
			}
		
		char suitChar = shortName.charAt(1);
		int suit = 4;
		for (int i=0; i<4; i++)
			if (suitChar == SUIT_CHAR[i]) {
				suit = i;
				break;
			}
		
		if (rank == 13  ||  suit==4)
			throw new ParseException("Unrecognized short name: "+shortName);
		
		return getCard(rank, suit);
	}
	
	/**
	 * Not optimized. No error handling.
	 */
	public static final int[] getCardList(String spaceSeparatedShortNames) throws ParseException {
		int[] result = new int[(spaceSeparatedShortNames.length()+1)/3];
		StringTokenizer st = new StringTokenizer(spaceSeparatedShortNames, " ");
		for (int i=0; i<result.length; i++)
			result[i] = getFromShortName(st.nextToken());
		return result;
	}
	
	public static final int encodePair(int card1, int card2) {
		return (card1 << 8) | card2;
	}
	
	public static final int decodeCard1(int cardPair) {
		return cardPair >> 8;
	}
	
	public static final int decodeCard2(int cardPair) {
		return cardPair & 0xFF;
	}
}
