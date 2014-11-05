package com.holahmeds.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException {
		System.setProperty("javax.net.ssl.trustStore", "chatterClientKeyStore.jks");
		
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket) socketFactory.createSocket("localhost", 11234);
		
		Scanner gimi = new Scanner(System.in);
		PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
		
		new Thread(new ServerHandler(socket)).start();
		
		while(gimi.hasNext()) {
			output.println(gimi.nextLine());
		}
		
		socket.close();
		gimi.close();
	}
}
