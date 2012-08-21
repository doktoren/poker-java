package poker.statistics;


import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import poker.io.ReadWriteFile;
import poker.io.ReadWriteIntArray;
import poker.io.ReadWriteObject;
import poker.io.ReadWriteTreeMap;
import poker.representation.ParseException;
import poker.util.ParseMisc;


public class TableGameSet extends TimeCompare {

	public static final RW rw = new RW();
	
	public static final int MAX_SIZE = 1024;
	
	public final TableSetup tableSetup;

	private final TreeMap<TimeCompare, SavedGame> games = new TreeMap<TimeCompare, SavedGame>();

	public final ReadWriteFile rwf; // Not saved
	
	/**
	 * @param tableSetup
	 * @param time Defined as in GameSetup: "2006:06:08 17:52:32"
	 */
	public TableGameSet(TableSetup tableSetup, long time) {
		super(time);
		this.tableSetup = tableSetup;
		rwf = new ReadWriteFile(new File(fileName()), rw);
	}
	
	private SavedGame lookup(long time) throws ParseException {
		Iterator<Entry<TimeCompare, SavedGame>> it =
			games.subMap(new TimeCompare(time-1000), new TimeCompare(time+1000)).entrySet().iterator();
		if (!it.hasNext())
			throw new ParseException("Lookup of time "+ParseMisc.timeToString(time)+" failed");
		return it.next().getValue();
	}
	
	//206406:18 14:31:24 #10843375 $0.5 + $0 - Table #2 (Real Money).tgs
	//20640618 143124 #10843375 $0.5 + $0 - Table #2 (Real Money).tgs
	
	public void toHTML(StringBuffer html) throws ParseException {
		tableSetup.toHTML(html);
		html.append("Games:<UL>\n");
		Iterator<SavedGame> it = games.values().iterator();
		while (it.hasNext()) {
			SavedGame sg = it.next();
			int gameNumber = ParseMisc.parseInt(sg.gameSetup.optional.get(GameSetup.GAME_NUMBER));
			String time = sg.gameSetup.optional.get(GameSetup.TIME);
			html.append("<LI><A HREF='"+fileName()+"/"+time+"'>Game number "+gameNumber+"</A> at time "+time+"</LI>\n");
		}
		html.append("</UL>\n");
	}
	
	public void toHTML(StringBuffer html, String time) throws ParseException {
		lookup(ParseMisc.timeStringToLong(time)).toHTML(html);
	}
	
	public static TableGameSet load(File file) throws ParseException, IOException {
		return (TableGameSet)new ReadWriteFile(file, rw).load();
	}
	
	public void save(String directory) throws ParseException, IOException {
		new ReadWriteFile(new File(directory+'/'+fileName()), rw).save(this);
	}
	
	public String fileName() {
		return super.toString()+' '+tableSetup.optional.get(TableSetup.TABLE_NAME)+".tgs";
	}
	
	public boolean isFull() {
		return games.size() == MAX_SIZE;
	}
	
	public void addSavedGame(SavedGame game) throws ParseException {
		games.put(new TimeCompare(TimeCompare.getTime(game.gameSetup)), game);
	}

	public static class RW extends ReadWriteObject {
	
		@Override
		public Class getObjectClass() {
			return TableGameSet.class;
		}

		@Override
		public int size(Object obj) {
			TableGameSet tgs = (TableGameSet)obj;
			int result = TimeCompare.rw.size(obj);
			result += TableSetup.rw.size(tgs.tableSetup);
			result += 4+4*MAX_SIZE;

			ReadWriteTreeMap rwTM = new ReadWriteTreeMap(TimeCompare.rw, new SavedGame.RW(tgs.tableSetup));
			result += rwTM.size(tgs.games);
			return result;
		}

		@Override
		public void myWrite(ByteBuffer bb, Object obj) throws ParseException {
			TableGameSet tgs = (TableGameSet)obj;
			ReadWriteTreeMap rwTM = new ReadWriteTreeMap(TimeCompare.rw, new SavedGame.RW(tgs.tableSetup));
			int[] offsets = new int[MAX_SIZE];
			rwTM.setOffsets(tgs.games, offsets);
			
			TimeCompare.rw.write(bb, obj);
			TableSetup.rw.myWrite(bb, tgs.tableSetup);
			ReadWriteIntArray.write(bb, offsets);
			rwTM.myWrite(bb, tgs.games);
		}

		@Override
		public Object read(ByteBuffer bb, int maxSize) throws ParseException {
			TimeCompare tc = (TimeCompare)TimeCompare.rw.read(bb);
			TableGameSet tgs = new TableGameSet((TableSetup)TableSetup.rw.read(bb, 10000), tc.time);
			ReadWriteTreeMap rwTM = new ReadWriteTreeMap(TimeCompare.rw, new SavedGame.RW(tgs.tableSetup));
			ReadWriteIntArray.read(bb);
			rwTM.read(bb, tgs.games, 0x7FFFFFFF);
			return tgs;
		}

		
	}
}
