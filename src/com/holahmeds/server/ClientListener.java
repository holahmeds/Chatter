package com.holahmeds.server;

import java.io.IOException;
import java.net.SocketException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class ClientListener implements Runnable {
	SSLServerSocket socket;
	
	@Override
	public void run() {
		SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
			socket = (SSLServerSocket) socketFactory.createServerSocket(Server.SERVER_LISTEN_PORT);
			Server.log("Listening on port "+socket.getLocalPort());
			while(true) {
				try {
					new Thread(new ClientHandler(socket.accept())).start();
				} catch (SocketException e) {
					Server.log("Listen socket closed");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
