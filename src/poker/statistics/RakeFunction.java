package poker.statistics;

public abstract class RakeFunction {

	public abstract int calculate(int potSize);
	
	public int calculate(int potSize, int numPlayers) {
		return calculate(potSize);
	}
	
}
