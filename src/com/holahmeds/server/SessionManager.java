package com.holahmeds.server;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The session manager creates and stores session keys for users. Session 
 * keys are be set to expire after a certain amount of time with no activity.</p>
 */
public class SessionManager {
	private ConcurrentHashMap<String, String> sessionToUser;
	private ConcurrentHashMap<String, Long> userLastContact;
	
	private Random random;

	public final long sessionTimeout;

	public SessionManager(int timeout) {
		sessionToUser = new ConcurrentHashMap<String, String>();
		userLastContact = new ConcurrentHashMap<String, Long>();
		random = new Random();
		sessionTimeout = timeout;
	}
	public SessionManager() {
		this(5000);
	}

	/**
	 * Creates a session key for the specified username.This method does not
	 * verify if user exists or not.
	 * @param username
	 * @return
	 */
	public String createSessionKey(String username) {
		String key;
		do {
			key = String.valueOf(random.nextInt());
		} while (sessionToUser.containsKey(key));

		sessionToUser.put(key, username);
		userLastContact.put(username, System.currentTimeMillis());

		return key;
	}

	/**
	 * @param sessionKey
	 * @return The user this key was created for. Null if the session key does
	 * 		not have a key associated with it.
	 */
	public String getUser(String sessionKey) {
		String user = sessionToUser.get(sessionKey);

		if (isUserOnline(user)) {
			userLastContact.put(user, System.currentTimeMillis());
			return user;
		} else {
			return null;
		}
	}

	public boolean isUserOnline(String user) {
		if (user != null && userLastContact.containsKey(user)) {
			return (System.currentTimeMillis() -  userLastContact.get(user)) < sessionTimeout;
		} else {
			return false;
		}
	}

}
