package poker.statistics;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.TreeSet;

import poker.representation.ParseException;
import poker.util.ParseMisc;


/**
 * 
 * 
 * @author Jesper Kristensen
 */
public class Database {
	
	public static final byte[] bindTo = //{80,(byte)164,81,119};
	{89,(byte)150,(byte)143,(byte)153};
	//{89,(byte)150,(byte)158,(byte)218};
	
	public final String directory;
	
	private final TreeSet<TimeCompare> gameSets = new TreeSet<TimeCompare>(); 
	
	public Database(String directory) {
		this.directory = directory;
	}
	
	/**
	 * The games must be added in the order that they were played
	 */
	public void insertSavedGame(SavedGame game) throws ParseException {
		synchronized(gameSets) {
			//TableGameSet tgs = new TableGameSet(game.gameSetup.tableSetup, );
			long time = ParseMisc.timeStringToLong(game.gameSetup.requireOptional(GameSetup.TIME));
			TimeCompare tcFirst = new TimeCompare(time-24*60*60*1000);
			TimeCompare tcLast = new TimeCompare(time);
			Iterator it = gameSets.subSet(tcFirst, tcLast).iterator();
			boolean foundMatch = false;
			while (it.hasNext()) {
				TableGameSet tgs = (TableGameSet)it.next();
				if (tgs.isFull())
					continue;
				if (tgs.tableSetup.equals(game.gameSetup.tableSetup)) {
					assert !foundMatch;
					foundMatch = true;
					tgs.addSavedGame(game);
					break;
				}
			}
			if (!foundMatch) {
				TableGameSet tgs = new TableGameSet(game.gameSetup.tableSetup, time);
				tgs.addSavedGame(game);
				gameSets.add(tgs);
			}
		}
	}
	
	public void load() throws ParseException, IOException {
		synchronized(gameSets) {
			gameSets.clear();
			File[] files = new File(directory).listFiles();
			for (int i=0; i<files.length; i++)
				gameSets.add(TableGameSet.load(files[i]));
		}
	}
	
	public void save() throws ParseException, IOException {
		synchronized(gameSets) {
			Iterator<TimeCompare> it = gameSets.iterator();
			while (it.hasNext()) {
				TableGameSet tgs = (TableGameSet)it.next();
				tgs.save(directory);
			}
		}
	}
	
	private void toHTML(StringBuffer html) {
		Iterator<TimeCompare> it = gameSets.iterator();
		while (it.hasNext()) {
			TableGameSet tgs = (TableGameSet)it.next();
			html.append("<A HREF='"+tgs.fileName()+"'>"+tgs.fileName()+"</HTML><BR>\n");
		}
	}
	
	private int decodeHex(char c) {
		if ('0'<=c  &&  c<='9')
			return c-'0';
		if ('A'<=c  &&  c<='F')
			return c-'A'+10;
		if ('a'<=c  &&  c<='f')
			return c-'a'+10;
		throw new RuntimeException();
	}
	
	private String decode(String s) {
		byte[] b = new byte[s.length()];
		int index = 0;
		for (int i=0; i<s.length(); i++) {
			if (s.charAt(i)=='%') {
				int n = 16*decodeHex(s.charAt(++i));
				n += decodeHex(s.charAt(++i));
				b[index++] = (byte)n;
			} else {
				b[index++] = (byte)s.charAt(i);
			}
		}
		return new String(b, 0, index);
	}
	
	public void startServer() {
		new Thread() {
			public void run() {
				try {
					String newLine = new String(new byte[]{13,10});
					
					ServerSocket ss = new ServerSocket(80, 5, InetAddress.getByAddress(bindTo));
					//ServerSocket ss = new ServerSocket(80, 5, InetAddress.getLocalHost());
					System.out.println("Bound server socket to "+ss.getLocalSocketAddress());
					while (true) {
						Socket socket = ss.accept();
						try {
							System.out.println("Accepted new connection");
							byte[] data = new byte[2000];
							int bytesRead = 0;
							while (true) {
								int n = socket.getInputStream().read(data, bytesRead, data.length-bytesRead);
								if (n <= 0)
									break;
								bytesRead += n;
								if (new String(data, 0, bytesRead).indexOf(newLine+newLine) != -1)
									break;
							}
							String s = new String(data, 0, bytesRead);
							//System.out.println(s);
							{
								int n = s.indexOf("GET ");
								if (n == -1)
									throw new IOException("\"GET\" not found in "+s);
								int m = s.indexOf(' ', n+5);
								if (m == -1)
									throw new IOException("' ' after \"GET\" not found in "+s);
								s = decode(s.substring(n+5, m));
								if ("favicon.ico".equals(s))
									continue;
							}
							
							TableGameSet tgs = null;
							String timeString = null;
							try {
								int n = s.indexOf('/');
								
								long time = ParseMisc.timeStringToLong(n==-1 ? s : s.substring(0, n));
								TimeCompare tc1 = new TimeCompare(time-1000);
								TimeCompare tc2 = new TimeCompare(time+1000);
								System.out.println(""+tc1+"    "+tc2);
								Iterator<TimeCompare> it = gameSets.subSet(tc1, tc2).iterator();
								while (it.hasNext()) {
									TableGameSet tmp = (TableGameSet)it.next();
									//System.out.println("### "+tmp);
									//System.out.println(s);
									//System.out.println(tmp.tableSetup.optional.get(TableSetup.TABLE_NAME));
									if (s.contains(tmp.tableSetup.optional.get(TableSetup.TABLE_NAME)))
										tgs = tmp;
								}
								
								if (n!=-1  &&  tgs!=null) {
									timeString = s.substring(n+1);
								}
							} catch (ParseException e) {
								System.out.println(e.toString());
							}
							
							StringBuffer html = new StringBuffer("<HTML><TITLE></TITLE><BODY>");
							if (tgs == null) {
								html.append("<H1>"+s+"</H1><BR>\n");
								toHTML(html);
							} else {
								if (timeString == null) {
									tgs.toHTML(html);
								} else {
									tgs.toHTML(html, timeString);
								}
							}
							html.append("</BODY></HTML>");
							
							String header = "HTTP/1.0 "+200+newLine+ 
								"Content-Lenght="+html.length()+newLine+
								newLine;
							
							//socket.getOutputStream().write((header+s).getBytes());
							//System.out.println(header+html);
							socket.getOutputStream().write((header+html).getBytes());
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {}
						} finally {
							socket.close();
						}
					}
					
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}
