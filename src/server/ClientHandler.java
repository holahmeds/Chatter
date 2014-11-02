package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
	private BufferedReader clientInput;
	private PrintWriter clientOutput;
	
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
			while (!(s = clientInput.readLine()).equals("exit"))
				clientOutput.println(s);
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
}
