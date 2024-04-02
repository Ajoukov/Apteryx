import java.io.*;
import java.util.*;

public class FileManager {
	static String[] openFiles = {
		"html/login.html",
		"html/crypt.js",
		"html/sendrequest.js",
		"html/index.html",
		"html/login-newuser.html"
	};

	static String[] protectedFiles = {
		"data/creds"
	};

	public static String[] getOpenFiles() {
		return openFiles;
	}
	public static String readFile(String path, boolean sudo) {
		Log.log("READING FILE:" + path);

		boolean valid = checkValidFile(path, sudo);
                if (!valid) {
			Log.log("INVALID FILE");
                        return null;
                }

                String rsp = "";
		try {
	                File f = new File(path);
	                Scanner readf = new Scanner(f);
	                while (readf.hasNextLine()) {
	                        String data = readf.nextLine();
	                        rsp += data + "\n";
	                }

	                readf.close();
		} catch (IOException e) {
			Log.log("FAILED TO READ FILE");
			return rsp;
		}
                return rsp;
	}

	public static boolean checkValidFile(String path, boolean sudo) {
                for (String s : openFiles) {
                        if (s.equals(path)) {
                                return true;
                        }
                }
		if (sudo) {
	                for (String s : protectedFiles) {
	                        if (s.equals(path)) {
	                                return true;
	                        }
	                }
		}
		return false;
	}

	public static int appendToFile(String path, String body, boolean sudo) {
		if (!sudo) {
			return -1;
		}
		try {
			File f = new File(path);
			f.createNewFile();
			FileWriter fr = new FileWriter(f, true);
			fr.write(body);
			fr.close();
			return 0;
		} catch (IOException e) {
			return -1;
		}
	}
}
