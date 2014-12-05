package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private BufferedReader clientInput;
	private OutputStream clientOutput;
	private SessionManager sessionManager;
	private String user;

	private static Random random;
	private static HashMap<String, LinkedBlockingQueue<String>> roomMembers;
	private static HashMap<String, LinkedBlockingQueue<String>> clientMessages;

	static {
		random = new Random();
		roomMembers = new HashMap<String, LinkedBlockingQueue<String>>();
		clientMessages = new HashMap<String, LinkedBlockingQueue<String>>();
	}

	public ClientHandler(Socket s, SessionManager sm) throws IOException {
		clientSocket = s;
		sessionManager = sm;
	}

	@Override
	public void run() {
		try {
			clientOutput = clientSocket.getOutputStream();
			clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String sessionKey = clientInput.readLine();
			user = sessionManager.getUser(sessionKey);

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
					sendMessages();
					break;
				case "get contacts":
					sendContacts();
					break;
				case "create room":
					createRoom(clientInput.readLine());
					break;
				case "add user to room":
					addUserToRoom(clientInput.readLine(), clientInput.readLine());
					break;
				case "remove user from room":
					removeUserFromRoom(clientInput.readLine());
					break;
				case "send room message":
					sendToRoom(clientInput.readLine(), clientInput.readLine());
					break;
				case "send room members":
					sendRoomMembers(clientInput.readLine());
					break;
				case "add contact":
					Database.addContact(user, clientInput.readLine());
					break;
				case "remove contact":
					Database.removeContact(user, clientInput.readLine());
					break;
				}
				
				write("done");
				clientInput.readLine();
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

		if (Database.validatePass(username, password)) {
			write(sessionManager.createSessionKey(username));
		} else {
			write("invalid login");
		}
		for (int i = 0; i < password.length; i++) {
			password[i] = 0;
		}
	}

	private void sendMessages() throws IOException {
		LinkedBlockingQueue<String> messages = clientMessages.get(user);
		
		while (!messages.isEmpty()) {
			write(messages.poll());
		}
	}
	
	private void write(String s) throws IOException {
		clientOutput.write(s.getBytes());
		clientOutput.write('\n');
	}
	
	private void sendContacts() throws IOException {
		ArrayList<String> contacts = Database.getContactsOfUser(user);
		for (String s : contacts) {
			write((userCheckContactOnline(user, s))
					? s + ":o"
					: s);
		}
	}
	
	private void createRoom(String toAdd) {
		String key;
		do {
			key = String.valueOf(random.nextInt());
		} while (roomMembers.containsKey(key));
		
		roomMembers.put(key, new LinkedBlockingQueue<String>());
		
		addUserToRoom(key, user);
		addUserToRoom(key, toAdd);
	}
	
	private void addUserToRoom(String room, String toAdd) {
		if ((toAdd.equals(user) || userCheckContactOnline(user, toAdd))
				&& !roomMembers.get(room).contains(toAdd)) {
			
			roomMembers.get(room).add(toAdd);
			clientMessages.get(toAdd).add("open room\n" + room);
		}
	}
	
	private void removeUserFromRoom(String room) {
		roomMembers.get(room).remove(user);
	}
	
	private void sendToRoom(String room, String message) {
		LinkedBlockingQueue<String> list = roomMembers.get(room);
		if (list.contains(user)) {
			for (String s : list) {
				clientMessages.get(s).add("message to room\n" + room + '\n'
						+ message + '\n' + user);
			}
		}
	}
	
	private void sendRoomMembers(String room) throws IOException {
		LinkedBlockingQueue<String> list = roomMembers.get(room);
		if (list.contains(user)) {
			for (String s : list) {
				write(s);
			}
		}
	}

	private boolean userCheckContactOnline(String user, String contact) {
		return Database.userHasContact(contact, user)
				&& sessionManager.isUserOnline(contact);
	}
}
