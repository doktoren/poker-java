package poker.io;

import java.nio.ByteBuffer;

import poker.representation.ParseException;


public class ReadWriteString extends ReadWriteObject {

	public static final ReadWriteString rw = new ReadWriteString();
	
	@Override
	public int size(Object obj) {
		String s = (String)obj;
		return 4+s.length();
	}

	@Override
	public void myWrite(ByteBuffer bb, Object obj) {
		String s = (String)obj;
		bb.putInt(s.length());
		bb.put(s.getBytes());
	}

	@Override
	public Object read(ByteBuffer bb, int maxSize) throws ParseException {
		int size = IOMisc.readCount(bb, maxSize-4);
		byte[] b = new byte[size];
		bb.get(b);
		return new String(b);
	}

	@Override
	public Class getObjectClass() {
		return String.class;
	}

}
