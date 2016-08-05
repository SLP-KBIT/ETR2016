package jp.etrobo.ev3.sample;

public class Logger {
	java.io.File file = null;
	java.io.PrintWriter pw = null;
	public Logger(String filename) {
		try {
			file = new java.io.File(filename);
			pw = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.FileWriter(file)));
		} catch (java.io.IOException e) {
			System.out.println("IOException : " + e);
		}
	}
	
	public void write(String str) {
		if (pw == null) return;
		pw.println(str);
	}
	
	public void close() {
		pw.flush();
		pw.close();
	}

}
