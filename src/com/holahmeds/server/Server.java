package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Server {
	static final int SERVER_LISTEN_PORT = 11234;
	
	private static ClientListener listener;
	
	private static SessionManager sessionManager;

	public static void main(String[] args) {
		BufferedReader gimi = new BufferedReader(new InputStreamReader(System.in));

		System.setProperty("javax.net.ssl.keyStore", "chatterServerKeyStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "somethingortheother");

		sessionManager = new SessionManager();
		listener = new ClientListener(sessionManager);
		new Thread(listener).start();

		try {
			while (true) {
				String[] command = gimi.readLine().split(" +");

				if (command.length == 1 && command[0].equals("stop")) {
					break;
				} else if(command.length == 3 && command[0].equals("pass")) {
					if (!Database.setUserPass(command[1], command[2].toCharArray())) {
						System.out.println("Invalid Username");
					}
				} else if(command.length == 3 && command[0].equals("useradd")) {
					Database.addUser(command[1], command[2].toCharArray());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Stopping server...");
			listener.close();
			try {
				DriverManager.getConnection("jdbc:derby:serverDB;shutdown=true");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static boolean userCheckContactOnline(String user, String contact) {
		return Database.userHasContact(user, contact)
				&& sessionManager.isUserOnline(contact);
	}
	
}
