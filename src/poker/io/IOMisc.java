package poker.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import poker.representation.ParseException;


public class IOMisc {
	
	private final static int STATUS_NOT_INITIALIZED = 0;
	private final static int STATUS_READ = 1;
	private final static int STATUS_WRITE = 2;
	
	public ByteBuffer bb;
	private final File file;
	private int status;
	
	public IOMisc(String fileName) {
		file = new File(fileName);
		file.getParentFile().mkdirs();
		status = STATUS_NOT_INITIALIZED;
	}
	
	private void write() {
		if (status != STATUS_WRITE)
			throw new RuntimeException("Not in write mode");	
	}
	
	private void read() {
		if (status != STATUS_READ)
			throw new RuntimeException("Not in read mode");
	}
	
	public static int readCount(ByteBuffer bb, int maxCount) throws ParseException {
		int result = bb.getInt();
		if (result<0  ||  maxCount<result)
			throw new ParseException("Read wrong count "+result);
		return result;
	}
	
	public void loadFile() throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[fis.available()];
		int n = fis.read(data);
		//System.out.println("Read "+n+" bytes from file "+file);
		bb = ByteBuffer.wrap(data);
		fis.close();
		status = STATUS_READ;
	}
	
	public void writeFile() throws IOException {
		write();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bb.array(), 0, bb.arrayOffset());
		fos.close();
	}
	
	public void allocateWriteBuffer(int size) {
		bb = ByteBuffer.wrap(new byte[size]);
		status = STATUS_WRITE;
	}	
	
	public static void main(String[] args) throws IOException {
		IOMisc test = new IOMisc("C:/poker statistics/rejected/hand_4526336.txt");
		test.loadFile();
	}
}
