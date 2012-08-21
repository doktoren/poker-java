package poker.advanced;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import poker.representation.PokerDeck;


/**
 * 
 * @author Jesper Kristensen
 */
public class HandTable {

	private static boolean isInitialized = false;
	private final static Entry[][] entries = new Entry[7][];
	
	// During init, these are used
	private final static HashMap map = new HashMap();
	
	public static void init() {
		PokerDeck deck = new PokerDeck();
		
		
		
	}
	
	Entry getEntry(int[] cards) {
		return null;
	}
	
	
	
	
	
	public static void loadFromFile(String fileName) throws IOException {
		
	}
	
	public static void saveToFile(String fileName) throws IOException {
		
	}
	
	public static void setAttachment(int[] cards, Serializable attachment) {
		
	}
	
	public static Serializable getAttachment(int[] cards) {
		return null;
	}
	
	public static int getHandValue(int[] cards) {
		return 0;
	}
}
