package poker.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import poker.representation.ParseException;


public class ReadWriteFile {

	private final File file;
	private final ReadWriteObject rwObject;
	
	public ReadWriteFile(File file, ReadWriteObject rwObject) {
		this.file = file;
		this.rwObject = rwObject;
	}
	
	public Object load() throws ParseException, IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			byte[] b = new byte[fis.available()];
			fis.read(b);
			ByteBuffer bb = ByteBuffer.wrap(b);
			return rwObject.read(bb);
		} finally {
			fis.close();
		}
	}
	
	public void save(Object obj) throws ParseException, IOException {
		byte[] b = new byte[rwObject.size(obj)];
		rwObject.write(ByteBuffer.wrap(b), obj);
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(b);
		} finally {
			fos.close();
		}
	}
}
