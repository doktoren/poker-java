package poker.representation;

import poker.util.Math;

public class PokerHands {

	public static final int HIGH_CARD = 0;
	public static final int A_PAIR = 1;
	public static final int TWO_PAIRS = 2;
	public static final int THREE_EQUAL = 3;
	public static final int STRAIGHT = 4;
	public static final int FLUSH = 5;
	public static final int FULL_HOUSE = 6;
	public static final int FOUR_EQUAL = 7;
	public static final int STRAIGHT_FLUSH = 8;
	
	/**
	 * @param handValue the value of a hand
	 * @return a number 0-8 giving the category of the hand
	 */
	public static final int getHandKind(int handValue) {
		return handValue >> 26;
	}
	
	/**
	 * Two hands will evaluate to the same value iff they are identical.
	 * 
	 * @param hand an array with the 7 cards
	 * @return an evaluation of the hand.
	 */
	public static final int calculateHandValue(int[] hand) {
		assert(hand.length == 7);
		int flush = 0;
		int straight = 0;
		int card_count_b0=0, card_count_b1=0, card_count_b2=0;
		
		for (int i=0; i<7; i++) {
			flush += (8+4+1)<<(8*(hand[i]&3));
			int tmp = 1<<(hand[i]>>2);
			straight |= tmp;
			
			if ((tmp & card_count_b0) != 0) {
				if ((tmp & card_count_b1) != 0) {
					card_count_b2 = tmp;// |= not nescessary
					card_count_b1 &= ~tmp;
					card_count_b0 &= ~tmp;
				} else {
					card_count_b1 |= tmp;
					card_count_b0 &= ~tmp;
				}
			} else {
				card_count_b0 |= tmp;
			}
		}
		
		assert(straight < 8192);
		assert(card_count_b0<8192  &&  card_count_b1<8192  &&  card_count_b2<8192);
		
		flush &= (1<<6) + (1<<14) + (1<<22) + (1<<30);
		
		{// Straight flush?
			if (flush != 0) {
				int straight_flush = 0;
				for (int i=0; i<7; i++) {
					if (((1<<(6+8*(hand[i]&3))) & flush) != 0)
						// only use hand of correct type
						straight_flush |= 1<<(hand[i]>>2);
				}
				if (STRAIGHT_TABLE[straight_flush] != 0)
					return (STRAIGHT_FLUSH << 26) | STRAIGHT_TABLE[straight_flush];
				
				// Modify flush to a potential return value
				flush = (FLUSH << 26) | REMOVE_ALL_BUT_FIVE_HIGH_BITS[straight_flush];
			}
		}
		
		{// 4 of a kind?
			if (card_count_b2 != 0)
				return (FOUR_EQUAL << 26) | card_count_b2;
		}
		
		int three_of_a_kind = REMOVE_ALL_BUT_HIGH_BIT[card_count_b0 & card_count_b1];
		
		
		{// Full house?
			if (three_of_a_kind!=0  &&  (card_count_b1 & ~three_of_a_kind)!=0)
				return (FULL_HOUSE << 26) | (three_of_a_kind << 13) | REMOVE_ALL_BUT_HIGH_BIT[card_count_b1 & ~three_of_a_kind];
		}
		
		{// Flush?
			// In the check of straight flush, flush is reassigned a value
			if (flush!=0)
				return flush;
		}
		
		{// Straight?
			if (STRAIGHT_TABLE[straight] != 0)
				return (STRAIGHT << 26) + STRAIGHT_TABLE[straight];
		}
		
		{// 3 of a kind?
			if (three_of_a_kind != 0) {
				// Only three of ONE kind and at most one of each other card
				card_count_b0 &= ~three_of_a_kind;
				// Ie. now 4 bits left in card_count_b0. remove 2 lowest!
				card_count_b0 &= card_count_b0-1;
				card_count_b0 &= card_count_b0-1;
				return (THREE_EQUAL << 26) + (three_of_a_kind << 13) + card_count_b0;
			}
		}
		
		{// 2 pairs?
			if ((card_count_b1 & (card_count_b1-1)) != 0) {
				int pairs = REMOVE_ALL_BUT_HIGH_BIT[card_count_b1];
				pairs = pairs | REMOVE_ALL_BUT_HIGH_BIT[card_count_b1 & ~pairs];
				// pairs now contains 2 bits representing the pairs
				
				// An eventual third pair can also be used as a high card
				card_count_b0 |= card_count_b1 & ~pairs;
				
				return (TWO_PAIRS << 26) + (pairs << 13) + REMOVE_ALL_BUT_HIGH_BIT[card_count_b0];
			}
		}
		
		{// A pair?
			if (card_count_b1 != 0) {
				// card_count_b0 contains the 5 single hand. Remove the 2 lowest.
				card_count_b0 &= card_count_b0-1;
				card_count_b0 &= card_count_b0-1;
				return (A_PAIR << 26) + (card_count_b1 << 13) + card_count_b0;
			}
		}
		
		// Only high card!
		// card_count_b0 contains the 7 single hand. Remove the 2 lowest.
		card_count_b0 &= card_count_b0-1;
		card_count_b0 &= card_count_b0-1;
		return card_count_b0;
	}
	
	
	public static final int[] getHandValues(int[] cards) {
		int num_players = ((cards.length-5) >> 1);
		int[] result = new int[num_players];
		
		int table_flush = 0;
		int table_straight = 0;
		
		int table_card_count_b0 = 0;
		int table_card_count_b1 = 0;
		int table_card_count_b2 = 0;
		
		for (int i=0; i<5; i++) {
			table_flush += (8+4+1)<<(8*(cards[i]&3));
			int tmp = 1<<(cards[i]>>2);
			table_straight |= tmp;
			
			if ((tmp & table_card_count_b0) != 0) {
				if ((tmp & table_card_count_b1) != 0) {
					table_card_count_b2 = tmp;// |= not nescessary
					table_card_count_b1 &= ~tmp;
					table_card_count_b0 &= ~tmp;
				} else {
					table_card_count_b1 |= tmp;
					table_card_count_b0 &= ~tmp;
				}
			} else {
				table_card_count_b0 |= tmp;
			}
		}
		
		
		for (int p=0; p<num_players; p++) {
			int flush = table_flush;
			int straight = table_straight;
			
			int card_count_b0 = table_card_count_b0;
			int card_count_b1 = table_card_count_b1;
			int card_count_b2 = table_card_count_b2;
			
			for (int i=5+2*p; i<7+2*p; i++) {
				flush += (8+4+1)<<(8*(cards[i]&3));
				int tmp = 1<<(cards[i]>>2);
				straight |= tmp;
				
				if ((tmp & card_count_b0) != 0) {
					if ((tmp & card_count_b1) != 0) {
						card_count_b2 = tmp;// |= not nescessary
						card_count_b1 &= ~tmp;
						card_count_b0 &= ~tmp;
					} else {
						card_count_b1 |= tmp;
						card_count_b0 &= ~tmp;
					}
				} else {
					card_count_b0 |= tmp;
				}
			}
			
			assert(straight < 8192);
			assert(card_count_b0<8192  &&  card_count_b1<8192  &&  card_count_b2<8192);
			
			flush &= (1<<6) + (1<<14) + (1<<22) + (1<<30);
			
			{// Straight flush?
				if (flush != 0) {
					// only use hand of correct type
					int straight_flush = 0;
					for (int i=0; i<5; i++)
						if (((1<<(6+8*(cards[i]&3))) & flush) != 0)
							straight_flush |= 1<<(cards[i]>>2);
					for (int i=5+2*p; i<7+2*p; i++)
						if (((1<<(6+8*(cards[i]&3))) & flush) != 0)
							straight_flush |= 1<<(cards[i]>>2);
					
					if (STRAIGHT_TABLE[straight_flush] != 0) {
						result[p] = 0x80000000 + STRAIGHT_TABLE[straight_flush];
						continue;
					}
					
					// Modify flush to a potential return value
					flush = 0x50000000 + REMOVE_ALL_BUT_FIVE_HIGH_BITS[straight_flush];
				}
			}
			
			{// 4 of a kind?
				if (card_count_b2 != 0) {
					result[p] = 0x70000000 + card_count_b2;
					continue;
				}
			}
			
			int three_of_a_kind = REMOVE_ALL_BUT_HIGH_BIT[card_count_b0 & card_count_b1];
			
			{// Full house?
				if (three_of_a_kind!=0  &&  (card_count_b1 & ~three_of_a_kind)!=0) {
					result[p] = 0x60000000 | (three_of_a_kind << 13) |
					REMOVE_ALL_BUT_HIGH_BIT[card_count_b1 & ~three_of_a_kind];
					continue;
				}
			}
			
			{// Flush?
				if (flush!=0) {
					// In the check of straight flush, flush is reassigned a value
					result[p] = flush;
					continue;
				}
			}
			
			{// Straight?
				if (STRAIGHT_TABLE[straight]!=0) {
					result[p] = 0x40000000 + STRAIGHT_TABLE[straight];
					continue;
				}
			}
			
			{// 3 of a kind?
				if (three_of_a_kind!=0) {
					// Only three of ONE kind and at most one of each other card
					card_count_b0 &= ~three_of_a_kind;
					// I.e. now 4 bits left in card_count_b0. remove 2 lowest!
					card_count_b0 &= card_count_b0-1;
					card_count_b0 &= card_count_b0-1;
					result[p] = 0x30000000 + (three_of_a_kind << 13) + card_count_b0;
					continue;
				}
			}
			
			{// 2 pairs?
				if ((card_count_b1 & (card_count_b1-1)) != 0) {
					int pairs = REMOVE_ALL_BUT_HIGH_BIT[card_count_b1];
					pairs = pairs | REMOVE_ALL_BUT_HIGH_BIT[card_count_b1 & ~pairs];
					// pairs now contains 2 bits representing the pairs
					
					// An eventual third pair can also be used as a high card
					card_count_b0 |= card_count_b1 & ~pairs;
					
					result[p] = 0x20000000 + (pairs << 13) + REMOVE_ALL_BUT_HIGH_BIT[card_count_b0];
					continue;
				}
			}
			
			{// A pair?
				if (card_count_b1 != 0) {
					// card_count_b0 contains the 5 single hand. Remove the 2 lowest.
					card_count_b0 &= card_count_b0-1;
					card_count_b0 &= card_count_b0-1;
					result[p] = 0x10000000 + (card_count_b1 << 13) + card_count_b0;
					continue;
				}
			}
			
			{ // Only high card!
				// card_count_b0 contains the 7 single hand. Remove the 2 lowest.
				card_count_b0 &= card_count_b0-1;
				card_count_b0 &= card_count_b0-1;
				result[p] = card_count_b0;
				continue;
			}
		}
		
		return result;
	}
	
	
	public static final String getHandValueDescription(int hand_value) {
		switch (hand_value >> 26) {
		case HIGH_CARD:
		{
			return "A high card: " + PokerMisc.RANK_NAME[Math.intLog(hand_value)];
		}
		case A_PAIR:
		{
			return "A pair of " + PokerMisc.RANK_NAME[Math.intLog((hand_value>>13)&((1<<13)-1))] + "'s";
		}
		case TWO_PAIRS:
		{
			int tmp = (hand_value>>13)&((1<<13)-1);
			int high_pair = Math.intLog(tmp);
			tmp -= 1<<high_pair;
			int low_pair = Math.intLog(tmp);
			return "2 pairs: " + PokerMisc.RANK_NAME[high_pair] + " and " + PokerMisc.RANK_NAME[low_pair];
		}
		case THREE_EQUAL:
		{
			return "Three " + PokerMisc.RANK_NAME[Math.intLog((hand_value>>13)&0x1FFF)] + "'s";
		}
		case STRAIGHT:
		{
			int tmp = hand_value & 0x1FFF;
			return "A straight " + PokerMisc.RANK_NAME[tmp-4] + " - " + PokerMisc.RANK_NAME[tmp];
		}
		case FLUSH:
		{
			// No information about suit!
			int tmp = hand_value & 0x1FFF;
			String result = "A flush " + PokerMisc.RANK_NAME[Math.intLog(tmp)];
			tmp -= 1<<Math.intLog(tmp);
			result += "," + PokerMisc.RANK_NAME[Math.intLog(tmp)];
			tmp -= 1<<Math.intLog(tmp);
			result += "," + PokerMisc.RANK_NAME[Math.intLog(tmp)];
			tmp -= 1<<Math.intLog(tmp);
			result += "," + PokerMisc.RANK_NAME[Math.intLog(tmp)];
			tmp -= 1<<Math.intLog(tmp);
			result += "," + PokerMisc.RANK_NAME[Math.intLog(tmp)];
			return result;
		}
		case FULL_HOUSE:
		{
			int high = Math.intLog((hand_value >> 13) & 0x1FFF);
			int low = Math.intLog(hand_value & 0x1FFF);
			return "A full house in " + PokerMisc.RANK_NAME[high] + "'s and " + PokerMisc.RANK_NAME[low] + "'s";
		}
		case FOUR_EQUAL:
		{
			return "Four " + PokerMisc.RANK_NAME[Math.intLog((hand_value>>13)&0x1FFF)] + "'s";
		}
		case STRAIGHT_FLUSH:
		{
			// No information about suit!
			int tmp = hand_value & 0x1FFF;
			if (tmp == 12) {
				return "A royal flush - sweet!";
			} else {
				return "A straight flush " + PokerMisc.RANK_NAME[tmp-4] + " - " + PokerMisc.RANK_NAME[tmp];
			}
		}
		}
		assert(false);
		return "Error";
	}
	
	
	static {
		initStraightTable();
		initRemoveAllButHighBit();
		initRemoveAllButFiveHighBits();
	}
	
	// Contains either the rank of the highest card in the straight, or 0 if no straight.
	private static final int[] STRAIGHT_TABLE = new int[8192];
	
	private static final void initStraightTable() {
		for (int i=0; i<8192; i++) {
			STRAIGHT_TABLE[i] = 0;
			
			// A-5 straight ?
			int pattern = 4096+15;
			if ((i&pattern)==pattern)
				STRAIGHT_TABLE[i] = 3;
			
			// Other straights?
			for (int lowest_card=8; lowest_card>=0; lowest_card--) {
				pattern = 31 << lowest_card;
				if ((i & pattern) == pattern) {
					STRAIGHT_TABLE[i] = lowest_card + 4;
					// We have found the best straight.
					break;
				}
			}
		}
	}
	
	
	private static final int[] REMOVE_ALL_BUT_HIGH_BIT = new int[8192];
	
	public static final void initRemoveAllButHighBit() {
		REMOVE_ALL_BUT_HIGH_BIT[0] = 0;
		for (int i=1; i<8192; i++)
			REMOVE_ALL_BUT_HIGH_BIT[i] = 1 << Math.intLog(i);
	}
	
	
	private static final int[] REMOVE_ALL_BUT_FIVE_HIGH_BITS = new int[8192];
	
	public static final void initRemoveAllButFiveHighBits() {
		for (int i=0; i<8192; i++) {
			int bit_count = 0;
			for (int j=i; j!=0; j&=j-1)
				bit_count++;
			
			REMOVE_ALL_BUT_FIVE_HIGH_BITS[i] = i;
			for (int j=5; j<bit_count; j++)
				REMOVE_ALL_BUT_FIVE_HIGH_BITS[i] &= REMOVE_ALL_BUT_FIVE_HIGH_BITS[i]-1;
		}
	}
	
	
	
	
}
