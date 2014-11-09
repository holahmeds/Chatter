package com.holahmeds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException {
		System.setProperty("javax.net.ssl.trustStore", "chatterClientKeyStore.jks");
		
		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocket socket = (SSLSocket) socketFactory.createSocket("localhost", 11234);
		
//		new Thread(new ServerHandler(socket)).start();
		BufferedReader gimi = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		OutputStream serverOutput = socket.getOutputStream();
		
		serverOutput.write("echo\necho this\n".getBytes());
		serverOutput.flush();
		String s;
		while ((s=gimi.readLine()) != null) {
			System.out.println(s);
		}
		
		socket.close();
		gimi.close();
	}
}
