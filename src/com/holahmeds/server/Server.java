package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class Server {
	static int SERVER_LISTEN_PORT;
	
	private static ClientListener listener;

	public static void main(String[] args) {
		BufferedReader gimi = new BufferedReader(new InputStreamReader(System.in));

		// set keystore pass
		System.setProperty("javax.net.ssl.keyStore", "serverKeyStore");
		System.out.println("Enter keystore password");
		System.setProperty("javax.net.ssl.keyStorePassword", String.valueOf(System.console().readPassword()));
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("server.properties"));
		} catch (IOException e1) {
		}
		SERVER_LISTEN_PORT = Integer.parseInt(properties.getProperty("listen port"));

		Database.init();
		listener = new ClientListener(new SessionManager(Integer.parseInt(properties.getProperty("session timeout"))));
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
					if (!Database.addUser(command[1], command[2].toCharArray())) {
						System.out.println("Username taken");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Stopping server...");
			listener.close();
			Database.close();
		}
	}
	
}
