package poker.statistics;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import poker.io.ReadWriteHashMap;
import poker.io.ReadWriteObject;
import poker.representation.ParseException;


public class TableSetup {

	public static final TableSetup.RW rw = new TableSetup.RW();
	
	// Optional values:
	//String pokerRoom = "Pacific Poker"; - Needed for unique identification of player
	//String tableName = Honolulu Hammer (Real Money)
	//String variant = $0.5/$1 Blinds No Limit Hold'em
	
	public static final String POKER_ROOM = "pokerRoom";
	public static final String TABLE_NAME = "tableName";
	public static final String VARIANT = "variant";
	
	public final HashMap<String, String> optional;
	
	public int bigBlind;
	public int rakeFunction;
	
	public TableSetup() {
		optional = new HashMap<String, String>();
	}
	
	public void toHTML(StringBuffer html) throws ParseException {
		html.append("Table information:<UL>\n");
		html.append("<LI>bigBlind = "+bigBlind+"</LI>\n");
		html.append("<LI>rakeFunction = "+RakeFunctions.getRakeDescription(rakeFunction)+"</LI>\n");
		Iterator<Entry<String,String>> it = optional.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			html.append("<LI>"+entry.getKey()+" = "+entry.getValue()+"</LI>\n");
		}
		html.append("</UL>\n");
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof TableSetup))
			return false;
		TableSetup ts = (TableSetup)obj;
		if (/*bigBlind!=ts.bigBlind  ||*/  rakeFunction!=ts.rakeFunction  ||  optional.size()!=ts.optional.size())
			return false;
		if (!optional.get(POKER_ROOM).equals(ts.optional.get(POKER_ROOM)))
			return false;
		if (!optional.get(TABLE_NAME).equals(ts.optional.get(TABLE_NAME)))
			return false;
		return true;
	}
	
	public int hashCode() {
		int result = bigBlind+331*rakeFunction;
		Iterator it = optional.keySet().iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			String value = optional.get(name);
			result ^= (name.hashCode()+1)*value.hashCode();
		}
		return result;
	}
	
	public static class RW extends ReadWriteObject {
		@Override
		public Class getObjectClass() {
			return TableSetup.class;
		}
		
		@Override
		public int size(Object obj) {
			return ReadWriteHashMap.rw.size(((TableSetup)obj).optional)+8;
		}
		
		@Override
		public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
			TableSetup ts = (TableSetup)obj;
			ReadWriteHashMap.rw.write(bb, ts.optional);
			bb.putInt(ts.bigBlind);
			bb.putInt(ts.rakeFunction);
		}
		
		@Override
		public Object read(ByteBuffer bb, int maxSize) throws ParseException {
			TableSetup result = new TableSetup();
			ReadWriteHashMap.rw.read(bb, result.optional, 10000);
			result.bigBlind = bb.getInt();
			result.rakeFunction = bb.getInt();
			return result;
		}
		
		@Override
		public Object read(ByteBuffer bb) throws ParseException {
			return read(bb, 10000);
		}
	}
}
