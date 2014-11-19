package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private BufferedReader clientInput;
	private OutputStream clientOutput;
	private SessionManager sessionManager;

	private static HashMap<String, LinkedBlockingQueue<String>> clientMessages;

	static {
		clientMessages = new HashMap<String, LinkedBlockingQueue<String>>();
	}

	public ClientHandler(Socket s, SessionManager sm) throws IOException {
		clientSocket = s;
		sessionManager = sm;

		clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		clientOutput = s.getOutputStream();
	}

	@Override
	public void run() {
		try {
			String sessionKey = clientInput.readLine();
			String user = sessionManager.getUser(sessionKey);

			String request = clientInput.readLine();
			if (request.equals("get session key")) {
				registerClient();
			} else if (user == null) {
				/* 
				 * client is trying to make requests with invalid session key
				 * or without logging in
				 */
				write("invalid session key");
				return;
			} else {
				// valid user making valid request
				
				if (!clientMessages.containsKey(user)) {
					clientMessages.put(sessionManager.getUser(sessionKey),
							new LinkedBlockingQueue<String>());
				}
				
				switch (request) {
				case "update":
					sendMessages(user);
					break;
				case "get contacts":
					sendContacts(user);
					break;
				}
				
				write("done");
			}
		} catch (IOException e) {
		} finally {
			this.close();
		}
	}

	void close() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void registerClient() throws IOException {
		String username = clientInput.readLine();
		char[] password = new char[Integer.parseInt(clientInput.readLine())];
		clientInput.read(password);

		if (Server.validatePass(username, password)) {
			write(sessionManager.createSessionKey(username));
		} else {
			write("invalid login");
		}
		for (int i = 0; i < password.length; i++) {
			password[i] = 0;
		}
	}

	private void sendMessages(String user) throws IOException {
		LinkedBlockingQueue<String> messages = clientMessages.get(user);
		
		while (!messages.isEmpty()) {
			write(messages.poll());
		}
	}
	
	private void write(String s) throws IOException {
		clientOutput.write(s.getBytes());
		clientOutput.write('\n');
	}
	
	private void sendContacts(String user) throws IOException {
		ArrayList<String> contacts = Server.getContactsOfUser(user);
		for (String s : contacts) {
			write((Server.userCheckContactOnline(user, s))
					? s + ":o"
					: s);
		}
	}
}
