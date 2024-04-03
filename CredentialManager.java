import java.io.*;
import java.util.*;
import java.net.*;
import com.sun.net.httpserver.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class CredentialManager {
	public static HashMap<Integer, Session> sessions = new HashMap<>();

	private static String sha1(String input) {
	try {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		byte[] hashBytes = digest.digest(input.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte b : hashBytes) {
			sb.append(String.format("%02x", b));
		}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getToken(HttpExchange exchange) throws IOException {
		Headers hs = exchange.getRequestHeaders();
		String token = "-1";
		List<String> cookies = hs.get("Cookie");
		if (cookies != null && cookies.size() > 0) {
			String cookie = cookies.get(0);
			int tokenLoc = cookie.indexOf("token=") + 6;
			if (tokenLoc != -1 && cookie.length() > tokenLoc) {
				int tokenEndLoc = cookie.indexOf(";", tokenLoc);
				if (tokenEndLoc != -1) {
					token = cookie.substring(tokenLoc, tokenEndLoc);
				} else {
					token = cookie.substring(tokenLoc);
				}
			}
		}
		Log.log("TOKEN:" + token);
		return Integer.parseInt(token);
	}

	public static boolean validSession(int token) {
		return validSession(token + "");
	}
	public static boolean validSession(String token) {
		Session session = sessions.get(Integer.parseInt(token));
		if (session == null) {
			return false;
                }
                if (session.timeElapsed() > 10000000) {
                        return false;
                }
                return true;
        }

        public static int logout(HttpExchange exchange) throws IOException {
		int token = getToken(exchange);
		if (token == -1) {
			Log.log("ALREADY LOGGED OUT");
			return -1;
		}
		sessions.remove(token);
		return 0;
	}

        public static int verifyCreds(HttpExchange exchange) throws IOException {
                Headers hs = exchange.getResponseHeaders();
                String body = Application.getContent(exchange);
                String all_creds = FileManager.readFile("data/creds", true);

                int colIndex = body.indexOf(":");
                String username = body.substring(0, colIndex);
                String password = body.substring(colIndex + 1);

                String[] split_creds = all_creds.split("\n");
                for (String s : split_creds) {
                        int secondCol = s.indexOf(":", 2);
                        String s_username = s.substring(2, secondCol);
                        String s_password = s.substring(secondCol + 1);
                        if (username.equals(s_username) && password.equals(s_password)) {
                                return createNewSession(username);
                        }
                }
		return -1;
        }


        public static int createNewCreds(HttpExchange exchange) throws IOException {
                Log.log("CREATE_NEW_CREDS: IN");
                Headers hs = exchange.getResponseHeaders();
                String body = Application.getContent(exchange);

                int colIndex = body.indexOf(":");
                if (colIndex == -1) {
			return -1;
                }
                String username = body.substring(0, colIndex);
                String pass_enc = body.substring(colIndex + 1);

                int count = body.length() - body.replace("\n", "").length();
                if (count > 0) {
			return -1;
                }
		String line = "0:" + username + ":" + pass_enc + "\n";
		FileManager.appendToFile("data/creds", line, true);

                int token = createNewSession(username);
                return token;
        }


        public static String getUserData(int token) {
		Session session = sessions.get(token);
		return session.getUsername();
	}


        public static int createNewSession(String username) {
                int token = -1;
                while (true) {
                        int new_token = (int) (Math.random() * Math.pow(2, 20));
                        if (!sessions.keySet().contains(new_token)) {
                                token = new_token;
                                break;
                        }
                }
                Session session = new Session(token, username);
                if (session == null) {
                        Log.log("Null session");
                        return -1;
                }
                Log.log("CREATED NEW TOKEN:" + token);
                sessions.put(token, session);

                return token;
        }
}
