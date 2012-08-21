package poker.advanced;

import java.io.Serializable;
import java.util.Arrays;

import poker.representation.PokerDeck;
import poker.util.Math;



public class Entry {
	
	/**
	 * next may either give an index in a table of entries of the value of the hand.
	 */
	private final int[] next = new int[52];
	
	private Serializable attachment;
	
	// This is a sorted standard representation of the cards
	private int[] cards;
	
	private int hashCode;
	
	/**
	 * The card array is copied.
	 */
	public Entry(int[] cards, int numCards) {
		this.cards = new int[numCards];
		for (int i=0; i<numCards; i++)
			this.cards[i] = cards[i];
		Mapping.applyMapping(this.cards);
		Arrays.sort(this.cards);
		
		initHashCode();
	}
	
	public int hashCode() {
		return hashCode;
	}
	
	public boolean equals(Object obj) {
		if (obj==null  ||  !(obj instanceof Entry))
			return false;
		
		Entry e = (Entry)obj;
		if (hashCode != e.hashCode  ||  cards.length != e.cards.length)
			return false;
		
		if (cards.length == 7) {
			for (int i=0; i<7; i++)
				if (cards[i] != e.cards[i])
					return false;
			return true;
		} else {
			// More advanced
		}
		return false;
	}
	
	private void initHashCode() {
		// hashCode is calculated from the standard representation of the cards
		hashCode = 1;
		for (int i=0; i<cards.length; i++)
			hashCode *= Math.primes[cards[i]];
	}
}
