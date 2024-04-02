import java.util.*;
import java.io.*;

public class Log {
	static File f = new File("log");

	public static void log(int i) {
		log(i + "");
        }
	public static void log(String s) {
		try {
			f.createNewFile();
			FileWriter fr = new FileWriter(f, true);
			fr.write(s + "\n");
			fr.close();
		} catch (IOException e) {
		}
	}

	public static void clearLog() {
		try {
			f.createNewFile();
			FileWriter fr = new FileWriter(f);
			fr.write("");
			fr.close();
			} catch (IOException e) {
		}
	}
}
