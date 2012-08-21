package poker.io;

import java.nio.ByteBuffer;

import poker.representation.ParseException;


public class ReadWriteIntArray {

	public static int size(int[] array) {
		return 4+4*array.length;
	}

	public static void write(ByteBuffer bb, int[] array) {
		bb.putInt(array.length);
		for (int i=0; i<array.length; i++)
			bb.putInt(array[i]);
	}

	public static int[] read(ByteBuffer bb, int maxSize) throws ParseException {
		int size = IOMisc.readCount(bb, maxSize);
		int[] result = new int[size];
		for (int i=0; i<size; i++)
			result[i] = bb.getInt();
		return result;
	}

	public static int[] read(ByteBuffer bb) throws ParseException {
		return read(bb, 0x7FFFFFFF);
	}
}
