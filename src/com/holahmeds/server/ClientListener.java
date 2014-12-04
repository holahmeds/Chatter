package com.holahmeds.server;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class ClientListener implements Runnable {
	private SSLServerSocket socket;
	private SessionManager sessionManager;
	private ExecutorService pool;
	
	public ClientListener(SessionManager sm) {
		sessionManager = sm;
		pool = Executors.newFixedThreadPool(10);
	}
	
	@Override
	public void run() {
		SSLServerSocketFactory socketFactory = 
				(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		
		try {
			socket = (SSLServerSocket) socketFactory
					.createServerSocket(Server.SERVER_LISTEN_PORT);
			
			System.out.println("Listening on port "+socket.getLocalPort());
			while(true) {
				try {
					pool.execute(
							new ClientHandler(socket.accept(), sessionManager));
				} catch (SocketException e) {
					System.out.println("Listen socket closed");
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
		pool.shutdown();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
