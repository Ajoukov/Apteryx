import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import com.sun.net.httpserver.*;

class Application {

	static String[] ignoreURIs = {"favicon.ico"};

	private static boolean checkCredentials(String user, String pwd) {
		return user.equals("admin") && pwd.equals("admin");
	}
	private static String getURI(HttpExchange e) {
		return e.getRequestURI().getPath();
	}
	private static String getQuery(HttpExchange e) {
		return e.getRequestURI().getQuery();
	}

	public static void main(String[] args) throws IOException {

		Log.clearLog();

		Log.log("Starting");

		int serverPort = 80;
		HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
		server.createContext("/", (exchange -> {
			Log.log("NEW EXCHANGE: " + getURI(exchange));
			handleRequest(exchange);
			exchange.close();
		}));

	        server.setExecutor(null);
	        server.start();
	}

	private static void sendResponse(HttpExchange exchange, String rsp) throws IOException {
		exchange.sendResponseHeaders(200, rsp.length());
		OutputStream output = exchange.getResponseBody();
		output.write(rsp.getBytes());
		output.flush();
		exchange.close();
	}


/*	private static String decode(final String encoded) {
		try {
			return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is a required encoding", e);
		}
	}


	public static Map<String, List<String>> splitQuery(String query) {
		if (query == null || "".equals(query)) {
			return Collections.emptyMap();
		}

		return Pattern.compile("&").splitAsStream(query)
		.map(s -> Arrays.copyOf(s.split("="), 2))
		.collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));
	}
*/
	private static void handleRequest(HttpExchange exchange) throws IOException {
		String URI = getURI(exchange);
		Headers hs = exchange.getRequestHeaders();

		for (String s : FileManager.getOpenFiles()) {
			if (URI.equals("/file/" + s)) {
				getFile(exchange);
				return;
			}
		}
		for (String s : ignoreURIs) {
			if (URI.equals("/" + s)) {
				ignoreExchange(exchange);
				return;
			}
		}

		if (URI.equals("/logout")) {
			CredentialManager.logout(exchange);
			sendResponse(exchange, "");
			return;
		}
		if (URI.equals("/login")) {
			sendToLogin(exchange);
			return;
		}
		if (URI.equals("/login/creds")) {
			int token = CredentialManager.verifyCreds(exchange);
			if (token != -1) {
				addHeader(exchange, "token", token + "");
				sendResponse(exchange, "");
			}
			sendError(exchange);
			return;
		}
		if (URI.equals("/login/creds/new")) {
			int token = CredentialManager.createNewCreds(exchange);
			if (token != -1) {
				addHeader(exchange, "token", token + "");
				sendResponse(exchange, "");
			}
			return;
		}
		if (URI.equals("/login/new")) {
			sendToNewLogin(exchange);
			return;
		}

		int token = CredentialManager.getToken(exchange);
		boolean validSession = CredentialManager.validSession(token);
		if (!validSession)
			sendToLogin(exchange);

		if (URI.equals("/userdata")) {
			sendUserData(exchange);
			return;
		}
		if (URI.equals("/")) {
			String rsp = FileManager.readFile("html/index.html", false);
			Session session = CredentialManager.sessions.get(token);
			String username = session.getUsername();
			addHeader(exchange, "username", username);
			sendResponse(exchange, rsp);
			return;
		}
		sendError(exchange);
		return;
	}

	private static void sendError(int err, HttpExchange exchange) throws IOException {
		Log.log("sendError: " + err);
		exchange.sendResponseHeaders(err, -1);
	}
	private static void sendError(HttpExchange exchange) throws IOException {
		Log.log("sendError: 400");
		exchange.sendResponseHeaders(400, -1);
	}

	private static void getFile(HttpExchange exchange) throws IOException {
		String URI = getURI(exchange);
		String fname = URI.substring(URI.indexOf("file/") + 5);
		String rsp = FileManager.readFile(fname, false);
		if (rsp == null) {
			sendError(exchange);
			return;
		}
		sendResponse(exchange, rsp);
		return;
	}

	private static void sendUserData(HttpExchange exchange) throws IOException {
		int token = CredentialManager.getToken(exchange);
		String username = CredentialManager.getUserData(token);
		String rsp = "";
		addHeader(exchange, "username", username);
		sendResponse(exchange, rsp);
	}
	private static void sendData(HttpExchange exchange) throws IOException {
		ApteryxProcessor ap = new ApteryxProcessor();
		String rsp = ap.getResults();
		sendResponse(exchange, rsp);
	}

	private static void sendToLogin(HttpExchange exchange) throws IOException {
		Log.log("Send to login page");
		String rsp = FileManager.readFile("html/login.html", false);
		sendResponse(exchange, rsp);
		return;
	}
	private static void ignoreExchange(HttpExchange exchange) throws IOException {
		Log.log("Ignored");
		sendResponse(exchange, "");
		return;
	}
	private static void sendToNewLogin(HttpExchange exchange) throws IOException {
		Log.log("Send to new user page");
		String rsp = FileManager.readFile("html/login-newuser.html", false);
		sendResponse(exchange, rsp);
		return;
	}

	public static String getContent(HttpExchange exchange) throws IOException {
            	InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
            	BufferedReader br = new BufferedReader(isr);

            	StringBuilder content = new StringBuilder();
            	String line;
            	while ((line = br.readLine()) != null) {
            	    	content.append(line);
            	}
            	br.close();
		return content.toString();
	}

	private static void addHeader(HttpExchange exchange, String key, String value) {
		Headers h = exchange.getResponseHeaders();
		h.add(key, value);
	}
}
