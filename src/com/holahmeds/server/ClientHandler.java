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
	
	/**
	 * Buffer used to store messages not yet sent to user.
	 */
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
			clientInput = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));

			// get session key and find user from that
			// user will be null if not logged in yet
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

				switch (request) {
				case "change password":
					changePass();
					break;
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
					addUserToRoom(clientInput.readLine(),
							clientInput.readLine());
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

	/**
	 * Verify login data and create a session key for user.
	 * This method handles reading its own input because it contains passwords.
	 * @throws IOException
	 */
	private void registerClient() throws IOException {
		String username = clientInput.readLine();
		char[] password = new char[Integer.parseInt(clientInput.readLine())];
		clientInput.read(password);

		if (Database.validatePass(username, password)) {
			String sessionKey = sessionManager.createSessionKey(username);
			
			write(sessionKey);
			
			// if first login then create buffer to store user messages
			if (!clientMessages.containsKey(user)) {
				clientMessages.put(sessionManager.getUser(sessionKey),
						new LinkedBlockingQueue<String>());
			}
		} else {
			write("invalid login");
		}
		
		// clear password
		for (int i = 0; i < password.length; i++) {
			password[i] = 0;
		}
	}

	/**
	 * Changes password of the user.
	 * This method handles reading its own input because it contains passwords.
	 * @throws IOException
	 */
	private void changePass() throws IOException {
		char[] oldPass = new char[Integer.parseInt(clientInput.readLine())];
		clientInput.read(oldPass);
		clientInput.readLine();
		char[] newPass = new char[Integer.parseInt(clientInput.readLine())];
		clientInput.read(newPass);

		if (Database.setUserPass(user, newPass, oldPass)) {
			write("success");
		} else {
			write("failure");
		}
		
		// clear both passwords
		for (int i = 0; i < oldPass.length; i++) {
			oldPass[i] = 0;
		}
		for (int i = 0; i < newPass.length; i++) {
			newPass[i] = 0;
		}
	}

	/**
	 * Send all messages stored in buffer for user.
	 * @throws IOException
	 */
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

	/**
	 * Sends a list of the user's contacts. If the contact is online then ":o"
	 * is appended to contact name.
	 * @throws IOException
	 */
	private void sendContacts() throws IOException {
		ArrayList<String> contacts = Database.getContactsOfUser(user);
		for (String s : contacts) {
			write((userCheckContactOnline(user, s))
					? s + ":o"
							: s);
		}
	}

	/**
	 * Creates a room and automatically adds the user
	 * and the contact toAdd to it.
	 * @param toAdd
	 */
	private void createRoom(String toAdd) {
		String key;
		do {
			key = String.valueOf(random.nextInt());
		} while (roomMembers.containsKey(key));

		roomMembers.put(key, new LinkedBlockingQueue<String>());

		addUserToRoom(key, user);
		addUserToRoom(key, toAdd);
	}

	/**
	 * Adds the user toAdd to a room.
	 * A user can only add someone to the room if they can see the
	 * contact online.
	 * @param room
	 * @param toAdd
	 */
	private void addUserToRoom(String room, String toAdd) {
		if ((toAdd.equals(user) || userCheckContactOnline(user, toAdd))
				&& !roomMembers.get(room).contains(toAdd)) {

			roomMembers.get(room).add(toAdd);
			clientMessages.get(toAdd).add("open room\n" + room);
		}
	}

	/**
	 * Removes user from a room. Users can only remove themselves from a room.
	 * @param room
	 */
	private void removeUserFromRoom(String room) {
		roomMembers.get(room).remove(user);
	}

	/**
	 * Send a message to a room.
	 * @param room
	 * @param message
	 */
	private void sendToRoom(String room, String message) {
		LinkedBlockingQueue<String> list = roomMembers.get(room);
		if (list.contains(user)) {
			for (String s : list) {
				clientMessages.get(s).add("message to room\n" + room + '\n'
						+ message + '\n' + user);
			}
		}
	}

	/**
	 * Sends a list of all members in a room. User can only see members of
	 * a room if they are in it.
	 * @param room
	 * @throws IOException
	 */
	private void sendRoomMembers(String room) throws IOException {
		LinkedBlockingQueue<String> list = roomMembers.get(room);
		if (list.contains(user)) {
			for (String s : list) {
				write(s);
			}
		}
	}

	/**
	 * Returns true if user1 can check if user2 is online.
	 * @param user1
	 * @param user2
	 * @return
	 */
	private boolean userCheckContactOnline(String user1, String user2) {
		return Database.userHasContact(user2, user1)
				&& sessionManager.isUserOnline(user2);
	}
}
