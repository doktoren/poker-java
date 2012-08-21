package poker.representation;

/**
 * index is used as the name for the index into the compact list of card pairs (of length 169).
 * fullIndex is used as the name for the index into the full list of all different card pairs (of length 1326).
 * 
 * @author Jesper Kristensen
 */
public final class PokerCardPairs {
	
	public static final boolean debug = false;
	
	/**
	 * @param index an index in [0..169[
	 * @return an encoded pair of cards where card1 > card2
	 */
	public static final int indexToEncodedPair(int index) {
		return GET_PAIR[index];
	}
	
	/**
	 * @param fullIndex an index in [0..1326[
	 * @return an encoded pair of cards where card1 > card2
	 */
	public static int fullIndexToEncodedPair(int fullIndex) {
		return FULL_INDEX_TO_PAIR[fullIndex];
	}
	
	/**
	 * @param cardPair an encoding of two cards
	 * @return an index in [0..169[
	 */
	public static int cardPairToIndex(int cardPair) {
		return GET_INDEX[(PokerMisc.decodeCard1(cardPair) << 6) | PokerMisc.decodeCard2(cardPair)];
	}

	/**
	 * @param card1
	 * @param card2
	 * @return an index in [0..169[
	 */
	public static int cardPairToIndex(int card1, int card2) {
		return GET_INDEX[(card1 << 6) | card2];
	}
	
	/**
	 * @param cardPair an encoding of two cards
	 * @return an index in [0..1326[
	 */
	public static int cardPairToFullIndex(int cardPair) {
		return GET_FULL_INDEX[(PokerMisc.decodeCard1(cardPair) << 6) | PokerMisc.decodeCard2(cardPair)];
	}
	
	/**
	 * @param card1
	 * @param card2
	 * @return an index in [0..1326[
	 */
	public static int cardPairToFullIndex(int card1, int card2) {
		return GET_FULL_INDEX[(card1 << 6) | card2];
	}
	
	/**
	 * @param index an index in [0..169[
	 * @return the number of pairs having this index. (c1,c2) and (c2,c1) are both counted.
	 */
	public static int getPairFrequency(int index) {
		return PAIR_FREQUENCY[index];
	}
	
	
	


	private static void debug(String name, int[] list) {
		if (!debug)
			return;
		StringBuffer sb = new StringBuffer(name+"("+list[0]);
		for (int i=1; i<list.length; i++)
			sb.append(", "+list[i]);
		System.out.println(sb.toString()+")");
	}
	
	private final static int[] GET_PAIR = new int[169];
	
	private static void initGetPair() {
		for (int i=0; i<13; i++)
			GET_PAIR[i] = PokerMisc.encodePair(4*i, 4*i+1);
		
		int index = 13;
		for (int i=4; i<52; i+=4)
			for (int j=0; j<i; j+=4)
				GET_PAIR[index++] = PokerMisc.encodePair(i, j);
		
		for (int i=4; i<52; i+=4)
			for (int j=1; j<i; j+=4)
				GET_PAIR[index++] = PokerMisc.encodePair(i, j);
		
		debug("GET_PAIR", GET_PAIR);
	}



	private final static int[] GET_INDEX = new int[64*53];

	private static int getIndex(int card1, int card2) {
		if (PokerMisc.getRank(card1) == PokerMisc.getRank(card2))
			return PokerMisc.getRank(card1);
		int result = (PokerMisc.getSuit(card1)==PokerMisc.getSuit(card2)) ? 13 : 13+12*13/2;
		//{1,0}=>0, {2,0}=>1, {2,1}=>2, {3,0}=>3, {3,1}=>4, {3,2}=>5, {4,0}=>6, ...
		int r1 = PokerMisc.getRank(card1);
		int r2 = PokerMisc.getRank(card2);
		if (r1<r2) {
			int tmp = r1;
			r1 = r2;
			r2 = tmp;
		}
		
		return result + (((r1-1)*r1)/2 + r2);
	}
	
	private static void initGetIndex() {
		for (int i=0; i<52; i++)
			for (int j=0; j<52; j++)
				GET_INDEX[(i<<6) | j] = (i==j) ? -1 : getIndex(i, j);
		for (int i=0; i<52; i++)
			GET_INDEX[(52<<6)|i] = GET_INDEX[(i<<6)|52] = 169;
		debug("GET_INDEX", GET_INDEX);
	}


	private static int[] PAIR_FREQUENCY = new int[169];

	private static void initPairFrequency() {
		for (int i=0; i<13; i++)
			PAIR_FREQUENCY[i] = 4*3;
		for (int i=13; i<13+78; i++)
			PAIR_FREQUENCY[i] = 8*1;
		for (int i=13+78; i<13+2*78; i++)
			PAIR_FREQUENCY[i] = 8*3;
		debug("PAIR_FREQUENCY", PAIR_FREQUENCY);
	}

	private static int[] GET_FULL_INDEX = new int[64*53];
	
	private static int toFullIndex(int card1, int card2) {
		if (card1 < card2) {
			int tmp = card1;
			card1 = card2;
			card2 = tmp;
		}
		return (((card1-1)*card1)>>1) + card2;
	}
	
	private static void initGetFullIndex() {
		for (int i=0; i<52; i++)
			for (int j=0; j<52; j++)
				GET_FULL_INDEX[(i<<6) | j] = (i==j) ? -1 : toFullIndex(i, j);
		for (int i=0; i<52; i++)
			GET_FULL_INDEX[(52<<6)|i] = GET_INDEX[(i<<6)|52] = 1326;
		debug("GET_FULL_INDEX", GET_FULL_INDEX);
	}


	private static int[] FULL_INDEX_TO_PAIR = new int[1326];

	private static void initFullIndexToPair() {
		for (int i=0; i<1326; i++)
			FULL_INDEX_TO_PAIR[i] = PokerMisc.encodePair(255, 255);
			for (int i=0; i<52; i++)
				for (int j=i+1; j<52; j++) {
					int index = GET_FULL_INDEX[(j<<6)|i];
					FULL_INDEX_TO_PAIR[index] = PokerMisc.encodePair(j, i);
				}
		debug("FULL_INDEX_TO_PAIR", FULL_INDEX_TO_PAIR);
	}
	
	static {
		initGetPair();
		initGetIndex();
		initPairFrequency();
		initGetFullIndex();
		initFullIndexToPair();
	}
	
	// #######################
	
	public static void test() {
		for (int i=0; i<169; i++)
			System.out.print(PokerMisc.getShortNamePair(indexToEncodedPair(i)) + "(" + getPairFrequency(i) + ") ");
		System.out.println();
	}
	
	public static void test2() {
		for (int i=0; i<1326; i++)
			System.out.println(PokerMisc.getShortNamePair(fullIndexToEncodedPair(i)) + " : " + cardPairToFullIndex(fullIndexToEncodedPair(i)));
	}
}
