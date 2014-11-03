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
import java.text.SimpleDateFormat;
import java.util.Date;

import net.crackstation.PasswordHash;

public class Server {
	static final int SERVER_LISTEN_PORT = 11234;
	private static SimpleDateFormat formatter;
	private static ClientListener listener;
	private static Connection dbCon;

	public static void main(String[] args) {
		formatter = new SimpleDateFormat("[MM/dd/yyyy h:mm:ss a]");
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

		listener = new ClientListener();
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
			log(e.toString());
		} catch (NullPointerException e) {
			log(e.toString());
		} finally {
			System.out.println("Stopping server...");
			listener.close();
			try {
				DriverManager.getConnection("jdbc:derby:serverDB;shutdown=true");
			} catch (SQLException e) {
				log(e.getMessage());
			}
		}
	}

	public static void log(String s) {
		System.out.println(formatter.format(new Date()) + s);
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
		} catch (SQLException e) {
			log(e.toString());
		} catch (NoSuchAlgorithmException e) {
			log(e.toString());
		} catch (InvalidKeySpecException e) {
			log(e.toString());
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

			if (result.next() && PasswordHash.validatePassword(password, result.getString("Password"))) {
				return true;
			}
		} catch (SQLException e) {
			log(e.toString());
		} catch (NoSuchAlgorithmException e) {
			log(e.toString());
		} catch (InvalidKeySpecException e) {
			log(e.toString());
		}

		return false;
	}
	
	public static boolean addUser(String user, char[] password) {
		try {
			Statement statement = dbCon.createStatement();
			int changes = statement.executeUpdate("INSERT INTO Users VALUES ('" + user
					+ "', '" + PasswordHash.createHash(password) + "')");
			
			if (changes == 1) {
				return true;
			}
		} catch (SQLException e) {
			log(e.toString());
		} catch (NoSuchAlgorithmException e) {
			log(e.toString());
		} catch (InvalidKeySpecException e) {
			log(e.toString());
		}
		
		return false;
	}
}
