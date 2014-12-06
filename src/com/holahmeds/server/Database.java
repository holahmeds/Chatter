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

	/**
	 * Connects to the database and prepares statements for use.
	 * If database does not exist this will create it.
	 */
	public static void init() {
		try {
			dbCon = DriverManager.getConnection("jdbc:derby:serverDB;create=true");
			if (!schemaExists()) {
				// new database
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

	/**
	 * Validates oldPassword and sets the password to newPassword
	 * @param user
	 * @param newPassword
	 * @param oldPassword
	 * @return
	 */
	public static boolean setUserPass(String user,
			char[] newPassword, char[] oldPassword) {
		
		if (validatePass(user, oldPassword)) {
			return setUserPass(user, newPassword);
		} else {
			return false;
		}
	}

	/**
	 * Validates user password.
	 * Returns false if password does not match the one in database or if
	 * user does not exist.
	 * @param user
	 * @param password
	 * @return
	 */
	public static boolean validatePass(String user, char[] password) {
		try {
			passHashGet.setString(1, user);
			ResultSet result = passHashGet.executeQuery();

			return result.next()
					&& PasswordHash.validatePassword(
							password, 
							result.getString("Pass_Hash"));
		} catch (SQLException | NoSuchAlgorithmException
				| InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Adds new user to database. Returns false if username already taken.
	 * @param user
	 * @param password
	 * @return
	 */
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

	/**
	 * Returns a list of the users contacts.
	 * @param user
	 * @return
	 */
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

	/**
	 * Returns true if user1 has user2 in their contacts list.
	 * @param user1
	 * @param user2
	 * @return
	 */
	public static boolean userHasContact(String user1, String user2) {
		try {
			contactFind.setString(1, user1);
			contactFind.setString(2, user2);

			ResultSet result = contactFind.executeQuery();
			return result.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/** 
	 * Adds user2 to the contacts list of user1.
	 * Does nothing if either user1 or user2 does not exist.
	 * @param user1
	 * @param user2
	 */
	public static void addContact(String user1, String user2) {
		try {
			if (!user1.equals(user2)) {
				contactInsert.setString(1, user1);
				contactInsert.setString(2, user2);
				contactInsert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes user2 from contacts list of user 1.
	 * Does nothing if either user1 or user2 does not exist.
	 * @param user1
	 * @param user2
	 */
	public static void removeContact(String user1, String user2) {
		try {
			contactDelete.setString(1, user1);
			contactDelete.setString(2, user2);
			contactDelete.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the schema USER_DATA exists in the database.
	 * @return
	 * @throws SQLException
	 */
	private static boolean schemaExists() throws SQLException {
		ResultSet schemas = dbCon.getMetaData().getSchemas(null, "USER_DATA");

		return schemas.next();
	}

	/**
	 * Disconnects from the database.
	 */
	public static void close() {
		try {
			DriverManager.getConnection("jdbc:derby:serverDB;shutdown=true");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
