package poker.representation;

public class ParseException extends Exception {
	
	public ParseException() {
		this("No description");
	}
	
	public ParseException(String descr) {
		super(descr);
	}
	
	public static void Assert(boolean b) throws ParseException {
		if (!b)
			throw new ParseException("Assertion failed");
	}
	
	public static void Assert(boolean b, String desc) throws ParseException {
		if (!b)
			throw new ParseException("Assertion failed: "+desc);
	}
}
