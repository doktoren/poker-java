package poker.io;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import poker.representation.ParseException;


public class ReadWriteHashMap extends ReadWriteObject {

	public static final ReadWriteHashMap rw = new ReadWriteHashMap(ReadWriteString.rw, ReadWriteString.rw);
	
	private final ReadWriteObject keyIO;
	private final ReadWriteObject valueIO;
	
	public ReadWriteHashMap(ReadWriteObject keyIO, ReadWriteObject valueIO) {
		this.keyIO = keyIO;
		this.valueIO = valueIO;
	}
	
	@Override
	public int size(Object obj) {
		HashMap map = (HashMap)obj;
		int result = 4;
		Iterator<Entry> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			result += keyIO.size(entry.getKey()) + valueIO.size(entry.getValue());
		}
		return result;
	}

	@Override
	public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
		HashMap map = (HashMap)obj;
		bb.putInt(map.size());
		Iterator<Entry> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			keyIO.write(bb, entry.getKey());
			valueIO.write(bb, entry.getValue());
		}
	}

	public Object read(ByteBuffer bb, HashMap map, int maxSize) throws ParseException {
		int size = IOMisc.readCount(bb, maxSize);
		maxSize -= 4;
		for (int i=0; i<size; i++) {
			Object key = keyIO.read(bb, maxSize);
			maxSize -= keyIO.size(key);
			Object value = valueIO.read(bb, maxSize);
			maxSize -= valueIO.size(key);
			map.put(key, value);
		}
		if (maxSize < 0)
			throw new ParseException("Too many bytes was read.");
		return map;
	}

	@Override
	public Object read(ByteBuffer bb, int maxSize) throws ParseException {
		return read(bb, new HashMap(), maxSize);
	}

	@Override
	public Class getObjectClass() {
		return HashMap.class;
	}

}
