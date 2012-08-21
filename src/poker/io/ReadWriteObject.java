package poker.io;

import java.nio.ByteBuffer;

import poker.representation.ParseException;


public abstract class ReadWriteObject {

	public abstract Class getObjectClass();
	
	public abstract int size(Object obj);
	
	public abstract void myWrite(ByteBuffer bb, Object obj) throws ParseException;
	
	public void write(ByteBuffer bb, Object obj) throws ParseException {
		int position = bb.position();
		myWrite(bb, obj);
		ParseException.Assert(position+size(obj) == bb.position());
	}
	
	public abstract Object read(ByteBuffer bb, int maxSize) throws ParseException;
	
	public Object read(ByteBuffer bb) throws ParseException  {
		return read(bb, 0x7FFFFFFF);
	}
	
}
