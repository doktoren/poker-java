package poker.representation;

import java.util.Random;

public final class PokerDeck {
	
	private long usedCards = 0;
	
	private final Random random;
	
	public PokerDeck() {
		random = new Random();
	}
	
	public PokerDeck(Random random) {
		this.random = random;
	}
	
	public int pickRandom() {
		int r;
		do {
			r = random.nextInt(52);
		} while (((1 << r) & usedCards) != 0);
		usedCards |= 1 << r;
		return r;
	}
	
	// Has no effect if card == 52
	public void remove(int card) {
		usedCards |= 1 << card;
	}
	
	public boolean contains(int card) {
		return (usedCards & (1 << card)) != 0;
	}
	
	// Has no effect if card == 52
	void putBack(int card) {
		assert((usedCards & (1 << card)) != 0  ||  card==52);
		usedCards &= ~(1<<card);
	}
}
