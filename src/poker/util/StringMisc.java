package poker.util;

public class StringMisc {

	public static String repeat(char fillChar, int count) {
		char[] tmp = new char[count];
		for (int i=0; i<tmp.length; i++)
			tmp[i] = fillChar;
		return new String(tmp);
	}
	
	public static String rightJustify(String s, int length, char fillChar) {
		if (s.length() >= length)
			return s;
		return repeat(fillChar, length-s.length())+s;
	}
	
	
}
