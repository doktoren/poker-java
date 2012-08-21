package poker.io;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import poker.representation.ParseException;


public class ReadWriteArray extends ReadWriteObject {

	public static final ReadWriteArray stringRW = new ReadWriteArray(ReadWriteString.rw);
	
	private final ReadWriteObject elementIO;
	
	public ReadWriteArray(ReadWriteObject elementIO) {
		this.elementIO = elementIO;
	}
	
	@Override
	public int size(Object obj) {
		Object[] array = (Object[])obj;
		int result = 4;
		for (int i=0; i<array.length; i++)
			result += elementIO.size(array[i]);
		return result;
	}

	@Override
	public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
		Object[] array = (Object[])obj;
		bb.putInt(array.length);
		for (int i=0; i<array.length; i++)
			elementIO.write(bb, array[i]);
	}

	@Override
	public Object read(ByteBuffer bb, int maxSize) throws ParseException {
		int size = IOMisc.readCount(bb, maxSize);
		maxSize -= 4;
		Object[] result = (Object[])Array.newInstance(elementIO.getObjectClass(), size);
		for (int i=0; i<size; i++) {
			Object obj = elementIO.read(bb, maxSize);
			//System.out.println(""+obj.getClass()+" "+elementIO.getObjectClass());
			result[i] = obj;
			maxSize -= elementIO.size(result[i]);
		}
		if (maxSize < 0)
			throw new ParseException("Too many bytes was read.");
		return result;
	}

	@Override
	public Class getObjectClass() {
		return elementIO.getObjectClass();//((Object[])Array.newInstance(elementIO.getClass(), 0)).getClass();
	}

}
