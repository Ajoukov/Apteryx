//package com.consulner.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import java.io.FileWriter;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.*;

import java.util.*;

class Application {

	static String[] validFiles = {"login.html", "crypt.js", "sendrequest.js"};
	static HashMap<Integer, Session> sessions = new HashMap<>();
//	static HashList<Integer> tokens = new HashList<Integer>();

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

		int serverPort = 80;
		HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
		server.createContext("/", (exchange -> {
			log("NEW EXCHANGE: " + getURI(exchange));
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


    public static Map<String, List<String>> splitQuery(String query) {
        if (query == null || "".equals(query)) {
            return Collections.emptyMap();
        }

        return Pattern.compile("&").splitAsStream(query)
            .map(s -> Arrays.copyOf(s.split("="), 2))
            .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));

    }

    public static String sha1(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(input.getBytes());

            // Convert byte array to hexadecimal string
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

    private static String decode(final String encoded) {
        try {
            return encoded == null ? null : URLDecoder.decode(encoded, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is a required encoding", e);
        }
    }

	private static void handleRequest(HttpExchange exchange) throws IOException {
		String URI = getURI(exchange);
		String query = getQuery(exchange);
		Headers hs = exchange.getRequestHeaders();

		if (URI.equals("/login")) {
			sendToLogin(exchange);
			return;
		}
		if (URI.equals("/file/crypt.js")) {
			getFile(exchange);
			return;
		}
		if (URI.indexOf("/file/sendrequest.js") == 0) {
			getFile(exchange);
			return;
		}
		if (URI.indexOf("/login/creds/new") == 0) {
			createNewCreds(exchange);
			return;
		}
		if (URI.indexOf("/login/creds") == 0) {
			verifyCreds(exchange);
			return;
		}
		if (URI.indexOf("/login/new") == 0) {
			sendToNewLogin(exchange);
			return;
		}

		String token = "-1";

		Map<String, List<String>> params = splitQuery(query);
		List<String> q = params.get("token");

		if (q != null) {
			token = q.get(0);
		}

		Session session = sessions.get(Integer.parseInt(token));
		if (session == null) {
			sendToLogin(exchange);
			return;
		}
		if (session.timeElapsed() > 10000000) {
			sendToLogin(exchange);
			return;
		}
		sendResponse(exchange, readFile("index.html"));
		exchange.close();
	}

	private static void log(int i) {
		log(i + "");
	}
	private static void log(String s) {
		try {
			File f = new File("log");
			f.createNewFile();
			FileWriter fr = new FileWriter(f, true);
			fr.write(s + "\n");
			fr.close();
		} catch (IOException e) {
		}
	}
	private static boolean verifyRequest(HttpExchange exchange) {
		Headers params = exchange.getRequestHeaders();
		String path = exchange.getHttpContext().getPath();
		List<String> authHeaders = params.get("Authorization");
		String authHeader = "";
		if (authHeaders != null) {
			authHeader = authHeaders.get(0);
		}
		if ("GET".equals(exchange.getRequestMethod()))
			return true;
		if ("PUT".equals(exchange.getRequestMethod())) {
			log(path);
			if (path.equals("/login/new"))
				return true;
			if (authHeader != null && !authHeader.equals("")) {
  //              		String credentials = new String(Base64.getDecoder().decode(encodedCredentials));
                		int colIndex = authHeader.indexOf(":");
				String username = authHeader.substring(0, colIndex);
                		String password = authHeader.substring(colIndex + 1);

                		if (checkCredentials(username, password)) {
                			return true;
                		}
        		}
			return false;
		}
		return false;
	}

	private static void returnError(int err, HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(err, -1);
	}
	private static void returnError(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(400, -1);
	}

	private static void getFile(HttpExchange exchange) throws IOException {
		Headers h = exchange.getResponseHeaders();
		String URI = getURI(exchange);
		String fname = URI.substring(URI.indexOf("file/") + 5);

		boolean valid = false;
		for (String s : validFiles) {
			if (s.equals(fname)) {
				valid = true;
				break;
			}
		}
		if (!valid) {
			returnError(exchange);
			return;
		}
		String rsp = readFile(fname);
		sendResponse(exchange, rsp);
		return;
	}

	private static String readFile(String fname) throws IOException {
		String rsp = "";
		File f = new File(fname);
		Scanner readf = new Scanner(f);
		while (readf.hasNextLine()) {
			String data = readf.nextLine();
			rsp += data + "\n";
		}

		readf.close();
		return rsp;
	}

	private static void sendToLogin(HttpExchange exchange) throws IOException {
		String rsp = readFile("login.html");
		sendResponse(exchange, rsp);
		return;
	}
	private static void sendToNewLogin(HttpExchange exchange) throws IOException {
		String rsp = readFile("login-newuser.html");
		sendResponse(exchange, rsp);
		return;
	}

	private static String getContent(HttpExchange exchange) throws IOException {
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

	private static void verifyCreds(HttpExchange exchange) throws IOException {
		Headers hs = exchange.getResponseHeaders();
		String body = getContent(exchange);
		String all_creds = readFile("creds");

		int colIndex = body.indexOf(":");
		String username = body.substring(0, colIndex);
		String password = body.substring(colIndex + 1);

		String[] split_creds = all_creds.split("\n");
		for (String s : split_creds) {
			int secondCol = s.indexOf(":", 2);
			String s_username = s.substring(2, secondCol);
			String s_password = s.substring(secondCol + 1);
			if (username.equals(s_username) && password.equals(s_password)) {
				addHeader(exchange, "token", createNewSession() + "");
				sendResponse(exchange, "");
			}
		}
		returnError(exchange);
	}

	private static void createNewCreds(HttpExchange exchange) throws IOException {
		Headers hs = exchange.getResponseHeaders();
		String body = getContent(exchange);

		int count = body.length() - body.replace("\n", "").length();
		if (count > 0) {
			returnError(exchange);
			return;
		}
		File f = new File("creds");
		f.createNewFile();
		FileWriter fr = new FileWriter(f, true);
		fr.write("0:" + body + "\n");
		fr.close();

		int token = createNewSession();
		addHeader(exchange, "token", token + "");
		sendResponse(exchange, "");
		return;
	}

	private static void addHeader(HttpExchange exchange, String key, String value) {
		Headers h = exchange.getResponseHeaders();
		h.add(key, value);
	}

	private static int createNewSession() {
		int token = -1;
		while (true) {
			int new_token = (int) (Math.random() * Math.pow(2, 20));
			if (!sessions.keySet().contains(new_token)) {
				token = new_token;
				break;
			}
		}
		Session session = new Session(token);
		if (session == null) {
			log("null session");
			return -1;
		}
		sessions.put(token, session);

		return token;
	}
}
