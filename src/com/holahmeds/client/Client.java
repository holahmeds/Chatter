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
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLSocketFactory;

import com.holahmeds.client.ui.Contacts;
import com.holahmeds.client.ui.LogIn;

public class Client {
	private static String serverAddress = "localhost";
	private static int port = 11234;
	
	private static SSLSocketFactory socketFactory;
	private static String sessionKey = "";
	
	private static LinkedBlockingQueue<Object> GUIInput;
	private static LogIn loginWindow;
	private static Contacts contactsWindow;
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		System.setProperty("javax.net.ssl.trustStore", "chatterClientKeyStore.jks");
		
		socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		
		GUIInput = new LinkedBlockingQueue<Object>();
		loginWindow = new LogIn(GUIInput);
		contactsWindow = new Contacts();
		
		if (!login(false, false)) {
			while (!login(true, false));
		}
		
		contactsWindow.setVisible(true);
		
		while (true) {
			EventQueue.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					try {
						contactsWindow.updateContacts(request("get contacts"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			Thread.sleep(500);
		}
	}
	
	public static boolean login(boolean retrying, boolean reloggingin) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = socketFactory.createSocket(serverAddress, port);
		PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
		
		loginWindow.show(retrying, reloggingin);
		serverOutput.println(sessionKey);
		serverOutput.println("get session key");
		
		serverOutput.println((String) GUIInput.take());
		serverOutput.println((int) GUIInput.take());
		serverOutput.println((char[]) GUIInput.take());
		
		BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String response = serverInput.readLine();
		
		if(response.equals("invalid login")) {
			return false;
		} else {
			sessionKey = response;
			return true;
		}
	}
	
	public static ArrayList<String> request(String s) throws UnknownHostException, IOException {
		Socket socket = socketFactory.createSocket(serverAddress, port);
		OutputStream serverOutput = socket.getOutputStream();
		BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		ArrayList<String> response = new ArrayList<String>();
		
		serverOutput.write(sessionKey.getBytes());
		serverOutput.write('\n');
		serverOutput.write(s.getBytes());
		serverOutput.write('\n');
		
		String temp;
		while (!(temp = serverInput.readLine()).equals("done")) {
			response.add(temp);
		}
		
		return response;
	}
}
