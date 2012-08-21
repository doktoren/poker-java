package poker.statistics;

import java.util.HashMap;

import poker.representation.ParseException;


public class RakeFunctions {

	private static final HashMap<Integer, RakeFunction> rakeFunctions =
		new HashMap<Integer, RakeFunction>();
	private static final HashMap<Integer, String> rakeDescriptions =
		new HashMap<Integer, String>();
	
	private static int addRakeFunction(int id, String desc, RakeFunction f) {
		Integer n = new Integer(id);
		if (rakeFunctions.containsKey(n))
			throw new RuntimeException("Reuse of id "+id);
		rakeFunctions.put(n, f);
		rakeDescriptions.put(n, desc);
		return id;
	}
	
	public static final int PACIFIC_POKER_NLHE = addRakeFunction(
			1, "Pacific Poker NLHE",
			new RakeFunction() {
				public int calculate(int potSize) {
					if (potSize > 80*100)
						return 4*100;
					return 5*(potSize / 100);
				}});
	
	public static final RakeFunction getRakeFunction(int id) throws ParseException {
		RakeFunction f = rakeFunctions.get(new Integer(id));
		if (f == null)
			throw new ParseException("No rake function with id "+id);
		return f;
	}
	
	public static String getRakeDescription(int id) throws ParseException {
		String description = rakeDescriptions.get(new Integer(id));
		if (description == null)
			throw new ParseException("No rake function with id "+id);
		return description;
	}
}
