package poker.io;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import poker.representation.ParseException;


public class ReadWriteTreeMap extends ReadWriteObject {

	public static final ReadWriteTreeMap rw = new ReadWriteTreeMap(ReadWriteString.rw, ReadWriteString.rw);
	
	private final ReadWriteObject keyIO;
	private final ReadWriteObject valueIO;
	
	public ReadWriteTreeMap(ReadWriteObject keyIO, ReadWriteObject valueIO) {
		this.keyIO = keyIO;
		this.valueIO = valueIO;
	}
	
	@Override
	public Class getObjectClass() {
		return TreeMap.class;
	}

	@Override
	public int size(Object obj) {
		TreeMap map = (TreeMap)obj;
		int result = 4;
		Iterator<Entry> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			result += keyIO.size(entry.getKey()) + valueIO.size(entry.getValue());
		}
		return result;
	}
	
	
	/**
	 * Returns the size
	 */
	public int setOffsets(TreeMap map, int[] offsets) {
		int result = 4;
		Iterator<Entry> it = map.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Entry entry = it.next();
			offsets[i++] = result;
			result += keyIO.size(entry.getKey()) + valueIO.size(entry.getValue());
		}
		return result;
	}

	@Override
	public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
		TreeMap map = (TreeMap)obj;
		bb.putInt(map.size());
		Iterator<Entry> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			keyIO.write(bb, entry.getKey());
			valueIO.write(bb, entry.getValue());
		}
	}

	@Override
	public Object read(ByteBuffer bb, int maxSize) throws ParseException {
		return read(bb, new TreeMap(), maxSize);
	}
	
	public Object read(ByteBuffer bb, TreeMap map, int maxSize) throws ParseException {
		int size = IOMisc.readCount(bb, maxSize);
		maxSize -= 4;
		for (int i=0; i<size; i++) {
			Object key = keyIO.read(bb, maxSize);
			maxSize -= keyIO.size(key);
			Object value = valueIO.read(bb, maxSize);
			maxSize -= valueIO.size(value);
			map.put(key, value);
		}
		if (maxSize < 0)
			throw new ParseException("Too many bytes was read.");
		return map;
	}
}
