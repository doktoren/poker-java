package poker.statistics;

import java.util.Stack;
import java.util.StringTokenizer;

import poker.representation.ParseException;
import poker.util.Pair;


public class MyStringTokenizer {

	private final StringTokenizer st;
	private final Stack<String> stack = new Stack<String>();
	
	public MyStringTokenizer(StringTokenizer st) {
		this.st = st;
	}
	
	public MyStringTokenizer(String s) {
		st = new StringTokenizer(s);
	}
	
	/**
	 * Pushes the token back so that it will be returned as the next token.
	 */
	public void pushBack(String token) {
		stack.push(token);
	}
	
	public boolean hasMoreTokens() {
		return !stack.empty()  ||  st.hasMoreTokens();
	}
	
	/**
	 * If the tokens doesn't match, no tokens are read.
	 */
	public void requireSpecificTokens(String[] requiredTokens) throws ParseException {
		Pair p = internalSpecificTokens(requiredTokens);
		if (p != null)
			throw new ParseException("Token "+p.o1+" was required, but "+p.o2+" was read.");
	}
	
	/**
	 * If the tokens doesn't match, no tokens are read.
	 */
	public boolean matchesSpecificTokens(String[] tokens) {
		return internalSpecificTokens(tokens) == null;
	}
	
	private Pair internalSpecificTokens(String[] tokens) {
		for (int i=0; i<tokens.length; i++) {
			if (!hasMoreTokens())
				return new Pair(tokens[i], null);
			String nextToken = _nextToken();
			if (!tokens[i].equals(nextToken)) {
				// Push back read tokens
				try {
					return new Pair(tokens[i], nextToken); 
				} finally {
					pushBack(nextToken);
					while (--i>=0)
						pushBack(tokens[i]);
				}
			}
		}
		return null;
	}
	
	/**
	 * If the token doesn't match, no token is read.
	 */	
	public void requireSpecificToken(String requiredToken) throws ParseException {
		requireToken();
		String nextToken = nextToken();
		if (!requiredToken.equals(nextToken)) {
			pushBack(nextToken);
			throw new ParseException("Token "+requiredToken+" was required, but "+nextToken+" was read.");
		}
	}
	
	public boolean matchesSpecificToken(String token) throws ParseException {
		requireToken();
		String nextToken = nextToken();
		if (!token.equals(nextToken)) {
			pushBack(nextToken);
			return false;
		}
		return true;
	}
	
	private void requireToken() throws ParseException {
		if (!hasMoreTokens())
			throw new ParseException("StringTokenizer is out of tokens.");
	}
	
	public String nextToken() throws ParseException {
		requireToken();
		return _nextToken();
	}
	
	// May only be called after requireToken has returned true
	private String _nextToken() {
		if (!stack.empty())
			return stack.pop();
		return st.nextToken();
	}
	
	public int readInt() throws ParseException {
		return Integer.parseInt(nextToken());
	}
	
	public String parseUntil(String terminateToken) throws ParseException {
		StringBuffer sb = new StringBuffer();
		requireToken();
		String lastToken = nextToken();
		while (!terminateToken.equals(lastToken)) {
			if (sb.length() != 0)
				sb.append(' ');
			sb.append(lastToken);
			lastToken = nextToken();
		}
		return sb.toString();
	}
}
