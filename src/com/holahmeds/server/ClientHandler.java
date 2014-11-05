package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private BufferedReader clientInput;
	private PrintWriter clientOutput;
	private String clientUsername;
	
	public ClientHandler(Socket s) throws IOException {
		clientSocket = s;
		Server.log("Client Connected at port "+clientSocket.getLocalPort());
		clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);
	}

	@Override
	public void run() {
		String s;
		try {
			loginClient();
			
			while (!(s = clientInput.readLine()).equals("exit")) {
				
			}
		} catch (IOException e) {
		} finally {
			clientOutput.println("exit");
			this.close();
		}
	}

	void close() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean loginClient() throws IOException {
		boolean result = false;
		
		while (!result) {
			clientOutput.println("send login details");
			
			// ignore all requests until client sends login details
			while(!clientInput.readLine().equals("sending login details"));

			clientUsername = clientInput.readLine();
			char[] password = new char[Integer.parseInt(clientInput.readLine())];
			clientInput.read(password);

			result = Server.validatePass(clientUsername, password);
			for (int i = 0; i < password.length; i++) {
				password[i] = 0;
			}
		}
		
		return result;
	}
	
	public String getUsername() {
		return clientUsername;
	}
}
