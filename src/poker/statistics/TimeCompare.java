package poker.statistics;

import java.nio.ByteBuffer;

import poker.io.ReadWriteObject;
import poker.representation.ParseException;
import poker.util.ParseMisc;


public class TimeCompare implements Comparable {
	
	public static final TimeCompare.RW rw = new TimeCompare.RW();
	
	protected final long time;
	
	public TimeCompare(long time) {
		this.time = time;
	}
	
	public int compareTo(Object arg0) {
		if (!(arg0 instanceof TimeCompare))
			throw new RuntimeException("Incomparable elements");
		long diff = time - ((TimeCompare)arg0).time;
		if (diff < 0)
			return -1;
		if (diff > 0)
			return 1;
		return 0;
	}
	
	public String toString() {
		return ParseMisc.timeToString(time);
	}
	
	public static long getTime(GameSetup gameSetup) throws ParseException {
		return ParseMisc.timeStringToLong(gameSetup.requireOptional(GameSetup.TIME));
	}
	
	public static class RW extends ReadWriteObject {

		@Override
		public Class getObjectClass() {
			return TimeCompare.class;
		}

		@Override
		public int size(Object obj) {
			return 8;
		}

		@Override
		public void myWrite(ByteBuffer bb, Object obj) {
			TimeCompare tc = (TimeCompare)obj;
			bb.putLong(tc.time);
		}

		@Override
		public Object read(ByteBuffer bb, int maxSize) throws ParseException {
			return new TimeCompare(bb.getLong());
		}
		
	}
}
