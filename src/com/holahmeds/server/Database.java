package com.holahmeds.server;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import net.crackstation.PasswordHash;

public class Database {
	private static Connection dbCon;

	private static PreparedStatement passHashChange;
	private static PreparedStatement passHashGet;
	private static PreparedStatement usersInsert;
//	private static PreparedStatement usersFind;
	private static PreparedStatement contactInsert;
	private static PreparedStatement contactFind;
	private static PreparedStatement contactDelete;

	public static void init() {
		try {
			dbCon = DriverManager.getConnection("jdbc:derby:serverDB;create=true");
			if (!schemaExists()) {
				Statement statement = dbCon.createStatement();
				statement.addBatch("CREATE SCHEMA user_data");
				statement.addBatch("SET SCHEMA user_data");
				statement.addBatch("CREATE TABLE Users (Username VARCHAR(30), Pass_Hash VARCHAR(120), PRIMARY KEY(Username))");
				statement.addBatch("CREATE TABLE Contact (Username VARCHAR(30), Contact VARCHAR(30), PRIMARY KEY(Username, Contact), FOREIGN KEY(Username) REFERENCES Users(Username), FOREIGN KEY(Contact) REFERENCES Users(Username))");
				statement.executeBatch();
				statement.close();
			} else {
				dbCon.createStatement().execute("set schema user_data");
			}

			passHashChange = dbCon.prepareStatement(
					"UPDATE Users SET Pass_Hash=? WHERE Username=?");
			passHashGet = dbCon.prepareStatement(
					"SELECT Pass_Hash FROM Users WHERE Username=?");
			usersInsert = dbCon.prepareStatement(
					"INSERT INTO Users VALUES (?, ?)");
//			usersFind = dbCon.prepareStatement(
//					"SELECT Username FROM Users WHERE Username LIKE ?");
			contactInsert = dbCon.prepareStatement(
					"INSERT INTO Contact VALUES (?, ?)");
			contactFind = dbCon.prepareStatement(
					"SELECT * FROM Contact WHERE Username LIKE ? AND Contact LIKE ?");
			contactDelete = dbCon.prepareStatement(
					"DELETE FROM Contact WHERE Username=? AND Contact=?");
		} catch (SQLException e) {
			e.printStackTrace();
			// Can't run the server without database so terminate
			System.exit(-1);
		}
	}

	static boolean setUserPass(String user, char[] password) {
		try {
			passHashChange.setString(1, PasswordHash.createHash(password));
			passHashChange.setString(2, user);

			int changes = passHashChange.executeUpdate();
			if (changes == 1) {
				return true;
			}
		} catch (SQLException
				| NoSuchAlgorithmException
				| InvalidKeySpecException e) {
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
			passHashGet.setString(1, user);
			ResultSet result = passHashGet.executeQuery();

			return result.next()
					&& PasswordHash.validatePassword(
							password, 
							result.getString("Pass_Hash"));
		} catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return false;
	}

	static boolean addUser(String user, char[] password) {
		try {
			usersInsert.setString(1, user);
			usersInsert.setString(2, PasswordHash.createHash(password));

			return usersInsert.executeUpdate() == 1;
		} catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static ArrayList<String> getContactsOfUser(String user) {
		ArrayList<String> contacts = new ArrayList<String>();
		try {
			contactFind.setString(1, user);
			contactFind.setString(2, "%");

			ResultSet result = contactFind.executeQuery();
			while (result.next()) {
				contacts.add(result.getString("Contact"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return contacts;
	}

	public static boolean userHasContact(String user, String contact) {
		try {
			contactFind.setString(1, user);
			contactFind.setString(2, contact);

			ResultSet result = contactFind.executeQuery();
			return result.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static void addContact(String user, String contact) {
		try {
			if (!user.equals(contact)) {
				contactInsert.setString(1, user);
				contactInsert.setString(2, contact);
				contactInsert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void removeContact(String user, String contact) {
		try {
			contactDelete.setString(1, user);
			contactDelete.setString(2, contact);
			contactDelete.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static boolean schemaExists() throws SQLException {
		ResultSet schemas = dbCon.getMetaData().getSchemas(null, "USER_DATA");

		return schemas.next();
	}

	public static void close() {
		try {
			DriverManager.getConnection("jdbc:derby:serverDB;shutdown=true");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
