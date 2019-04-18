package nesrs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileDumper {
	private PrintWriter writer;
	private String[] buffer = new String[100];
	private int index = 0;

	public FileDumper(String fileName) {
		try {
			writer = new PrintWriter(new File(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated constructor stub
	}

	public void dumpLine(String line) {
		if (index < buffer.length) {
			buffer[index++] = line;
		} else {
			for (int i = 0; i < buffer.length; i++) {
				writer.println(buffer[i]);
			}
			writer.flush();
			index = 0;
		}
	}

	public void close() {
		if (index > 0) {
			for (int i = 0; i < index; i++) {
				writer.println(buffer[i]);
			}
			writer.flush();
		}

		writer.close();
	}
}
