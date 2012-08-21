package poker.io;

import java.io.File;
import java.io.IOException;

import poker.representation.ParseException;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String fileName = "C:/tmp/test";
		
		String[] tmp = new String[]{"jesper", "torp", "kristensen"};
		ReadWriteFile rwf = new ReadWriteFile(new File(fileName), ReadWriteArray.stringRW);
		
		try {
			rwf.save(tmp);
			String[] result = (String[])rwf.load();
			for (int i=0; i<result.length; i++)
				System.out.println(result[i]);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
