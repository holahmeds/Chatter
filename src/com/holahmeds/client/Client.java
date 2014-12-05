package com.holahmeds.client;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLSocketFactory;

import com.holahmeds.client.ui.UIContacts;
import com.holahmeds.client.ui.UILogIn;

public class Client {
	private static String serverAddress = "localhost";
	private static int port = 11234;

	private static SSLSocketFactory socketFactory;
	private static String sessionKey = "";

	private static ArrayList<String> onlineContacts;
	private static ArrayList<String> offlineContacts;
	private static Object contactLock;
	
	private static LinkedBlockingQueue<Object> GUIInput;
	private static UILogIn loginWindow;
	private static UIContacts contactsWindow;
	
	private static HashMap<String, LinkedBlockingQueue<String>> rooms;

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		System.setProperty("javax.net.ssl.trustStore", "chatterClientKeyStore.jks");

		socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		
		onlineContacts = new ArrayList<String>();
		offlineContacts = new ArrayList<String>();
		contactLock = new Object();

		GUIInput = new LinkedBlockingQueue<Object>();
		loginWindow = new UILogIn(GUIInput);
		contactsWindow = new UIContacts();
		
		rooms = new HashMap<String, LinkedBlockingQueue<String>>();

		boolean flag = true;
		do {
			flag = login(!flag, false);
		} while (!flag);

		contactsWindow.setVisible(true);

		while (true) {
			ArrayList<String> contacts = request("get contacts");
			onlineContacts.clear();
			offlineContacts.clear();
			synchronized (contactLock) {
				for (String s : contacts) {
					String[] sd = s.split(":");

					if (sd.length == 2) {
						// contact is online
						onlineContacts.add(sd[0]);
					} else {
						// contact offline
						offlineContacts.add(sd[0]);
					}
				}
			}
			
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					contactsWindow.updateContacts();
				}
			});
			
			ArrayList<String> updates = request("update");
			Iterator<String> it = updates.iterator();
			while (it.hasNext()) {
				String s = it.next();
				String rid = it.next();
				if (s.equals("open room")) {
					rooms.put(rid, new LinkedBlockingQueue<String>());
					new Thread(new Room(rid, rooms.get(rid))).start();
				} else if (s.equals("message to room")) {
					rooms.get(rid).add(it.next() + ':' + it.next());
				}
			}

			Thread.sleep(500);
		}
	}

	public static boolean login(boolean retrying, boolean reloggingin) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = socketFactory.createSocket(serverAddress, port);
		PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				loginWindow.show(retrying, reloggingin);
			}
		});
		serverOutput.println(sessionKey);
		serverOutput.println("get session key");

		serverOutput.println((String) GUIInput.take());
		serverOutput.println((int) GUIInput.take());
		serverOutput.println((char[]) GUIInput.take());

		BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String response = serverInput.readLine();

		if(response.equals("invalid login")) {
			serverOutput.println("done");
			return false;
		} else {
			sessionKey = response;
			serverOutput.println("done");
			return true;
		}
	}

	public static ArrayList<String> request(String s) throws IOException, InterruptedException {
		Socket socket = socketFactory.createSocket(serverAddress, port);
		OutputStream serverOutput = socket.getOutputStream();
		BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		ArrayList<String> response = new ArrayList<String>();

		serverOutput.write(sessionKey.getBytes());
		serverOutput.write('\n');
		serverOutput.write(s.getBytes());
		serverOutput.write('\n');

		String temp = serverInput.readLine();
		if (temp.equals("invalid session key")) {
			boolean flag = true;
			do {
				try {
					flag = login(!flag, true);
				} catch (UnknownHostException e) {
					System.out.println("Unable to reconnect to  server");
				}
			} while (!flag);
		} else {
			while (!temp.equals("done")) {
				response.add(temp);
				temp = serverInput.readLine();
			}
		}
		serverOutput.write("done\n".getBytes());

		return response;
	}

	public static String[] getOnlineContacts() {
		synchronized (contactLock) {
			return onlineContacts.toArray(new String[onlineContacts.size()]);
		}
	}
	public static String[] getOfflineContacts() {
		synchronized (contactLock) {
			return offlineContacts.toArray(new String[offlineContacts.size()]);
		}
	}
}
