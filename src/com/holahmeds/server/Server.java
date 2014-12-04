package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.crackstation.PasswordHash;

public class Server {
	static final int SERVER_LISTEN_PORT = 11234;
	
	private static ClientListener listener;
	private static Connection dbCon;
	private static SessionManager sessionManager;

	public static void main(String[] args) {
		BufferedReader gimi = new BufferedReader(new InputStreamReader(System.in));

		System.setProperty("javax.net.ssl.keyStore", "chatterServerKeyStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "somethingortheother");

		try {
			dbCon = DriverManager.getConnection("jdbc:derby:serverDB");
			dbCon.createStatement().execute("set schema user_data");
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		sessionManager = new SessionManager();
		listener = new ClientListener(sessionManager);
		new Thread(listener).start();

		try {
			while (true) {
				String[] command = gimi.readLine().split(" +");

				if (command.length == 1 && command[0].equals("stop")) {
					break;
				} else if(command.length == 3 && command[0].equals("pass")) {
					if (!setUserPass(command[1], command[2].toCharArray())) {
						System.out.println("Invalid Username");
					}
				} else if(command.length == 3 && command[0].equals("useradd")) {
					addUser(command[1], command[2].toCharArray());
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

	private static boolean setUserPass(String user, char[] password) {
		try {
			Statement statement = dbCon.createStatement();
			int changes = statement.executeUpdate("UPDATE Users SET Pass_Hash='"
					+ PasswordHash.createHash(password)
					+ "' WHERE Username='" + user + '\'');

			if (changes == 1) {
				return true;
			}
			
			statement.close();
		} catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static boolean setUserPass(String user, char[] password, char[] oldPassword) {
		if (validatePass(user, oldPassword)) {
			return setUserPass(user, password);
		} else {
			return false;
		}
	}

	public static boolean validatePass(String user, char[] password) {
		try {
			Statement statement = dbCon.createStatement();
			ResultSet result = statement.executeQuery(
					"SELECT Pass_Hash FROM Users WHERE Username='" + user + '\'');

			if (result.next() && PasswordHash.validatePassword(password, result.getString("Pass_Hash"))) {
				return true;
			}
			
			statement.close();
		} catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	public static boolean addUser(String user, char[] password) {
		try {
			Statement statement = dbCon.createStatement();
			int changes = statement.executeUpdate(
					"INSERT INTO Users VALUES ('" + user + "', '"
					+ PasswordHash.createHash(password) + "')");
			
			if (changes == 1) {
				return true;
			}
			
			statement.close();
		} catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static ArrayList<String> getContactsOfUser(String user) {
		ArrayList<String> contacts = new ArrayList<String>();
		try {
			Statement statement = dbCon.createStatement();
			ResultSet result = statement.executeQuery(
					"SELECT Contact FROM Contact WHERE Username='" + user
					+ '\'');
			
			while (result.next()) {
				contacts.add(result.getString("Contact"));
			}
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return contacts;
	}
	
	public static boolean userCheckContactOnline(String user, String contact) {
		try {
			Statement statement = dbCon.createStatement();
			ResultSet result = statement.executeQuery(
					"SELECT Username FROM Contact WHERE Contact='" + user
					+ "' AND Username='" + contact + '\'');
			
			if (result.next()) {
				return sessionManager.isUserOnline(contact);
			}
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	public static void addContact(String user, String contact) {
		try {
			Statement statement = dbCon.createStatement();
			ResultSet checkExist = statement.executeQuery(
					"SELECT Username FROM Users WHERE Username='" + contact + '\'');
			
			if (checkExist.next() && !getContactsOfUser(user).contains(contact)) {
				statement = dbCon.createStatement();
				statement.executeUpdate(
						"INSERT INTO Contact VALUES ('" + user + "','" + contact + "')");
			}
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeContact(String user, String contact) {
		try {
			Statement statement = dbCon.createStatement();
			statement.executeUpdate(
					"DELETE FROM Contact WHERE Username='" + user
					+ "' AND Contact='" + contact + '\'');
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
