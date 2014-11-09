package com.holahmeds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private BufferedReader clientInput;
	private OutputStream clientOutput;

	public ClientHandler(Socket s) throws IOException {
		clientSocket = s;
		clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		clientOutput = s.getOutputStream();
	}

	@Override
	public void run() {
		try {
			String request = clientInput.readLine();
			switch (request) {
			case "echo":
				clientOutput.write(clientInput.readLine().getBytes());
				break;
			case "get session key":
				registerClient();
				break;
			}
		} catch (IOException e) {
		} finally {
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

	private void registerClient() throws IOException {
		String username = clientInput.readLine();
		char[] password = new char[Integer.parseInt(clientInput.readLine())];
		clientInput.read(password);

		if (Server.validatePass(username, password)) {
			clientOutput.write(createSessionKey(username).getBytes());
		} else {
			clientOutput.write("invalid login".getBytes());
		}
		for (int i = 0; i < password.length; i++) {
			password[i] = 0;
		}
	}

	private String createSessionKey(String user) {
		String key = null;
		while (key == null || Server.sessionToUser.containsKey(key)) {
			key = String.valueOf(Server.random.nextInt());
		}

		Server.sessionToUser.put(key, user);

		return key;
	}
}
