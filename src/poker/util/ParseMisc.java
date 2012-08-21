package poker.util;

import java.util.Calendar;

import poker.representation.ParseException;


public class ParseMisc {

	/**
	 * s may be preceded by a '$'
	 */
	public static int readChipCount(String s) throws ParseException {
		if (s.length() == 0)
			throw new ParseException("readChipCount on empty string");
		if (s.charAt(0) == '$')
			return readChipCount(s.substring(1));
		int n = s.indexOf('.');
		if (n+1 == s.length())
			throw new ParseException("No digit after '.': "+s);
		int cents = n==-1 ? 0 : (n+2==s.length() ? 10 : 1)*parseInt(s.substring(n+1));
		if (cents<0  ||  99<cents)
			throw new ParseException("The string "+s+" has an invalid number of cents");
		int dollars = parseInt(n==-1 ? s : s.substring(0, n));
		return 100*dollars + cents;
	}
	
	/**
	 * A chipCount of 250 is written $2.5, not $2.50
	 */
	public static String chipCountToString(int chipCount) {
		if (chipCount == -1)
			return "fold";
		if (chipCount<0)
			throw new RuntimeException("Negative chip count.");
		int cents = chipCount%100;
		if (cents == 0)
			return ""+(chipCount/100);
		return ""+(chipCount/100)+(cents<10 ? ".0" : ".")+(cents%10==0 ? cents/10 : cents);
	}
	
	public static int parseInt(String s) throws ParseException {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new ParseException(e.toString());
		}
	}
	
	public static long timeStringToLong(String time) throws ParseException {
		if (time.length() < 19)
			throw new ParseException("The string "+time+" is too short.");
		Calendar c = Calendar.getInstance();
		try {
			c.set(ParseMisc.parseInt(time.substring(0,4)),
					ParseMisc.parseInt(time.substring(5,7)),
					ParseMisc.parseInt(time.substring(8,10)),
					ParseMisc.parseInt(time.substring(11,13)),
					ParseMisc.parseInt(time.substring(14,16)),
					ParseMisc.parseInt(time.substring(17,19)));
		} catch (ParseException e) {
			System.out.println("ParseException for time string '"+time+"'");
			throw e;
		}
		return c.getTimeInMillis();
	}
	
	/**
	 * "2006:06:08 17:52:32";
	 */
	public static String timeToString(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		
		return ""+c.get(Calendar.YEAR)+';'+
			StringMisc.rightJustify(""+c.get(Calendar.MONTH), 2, '0')+';'+
			StringMisc.rightJustify(""+c.get(Calendar.DAY_OF_MONTH), 2, '0')+' '+
			StringMisc.rightJustify(""+c.get(Calendar.HOUR_OF_DAY), 2, '0')+';'+
			StringMisc.rightJustify(""+c.get(Calendar.MINUTE), 2, '0')+';'+
			StringMisc.rightJustify(""+c.get(Calendar.SECOND), 2, '0');
	}
}
