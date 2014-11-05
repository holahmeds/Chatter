package com.holahmeds.client;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.holahmeds.client.ui.LogIn;

public class ServerHandler implements Runnable {
	private BufferedReader serverInput;
	private PrintWriter serverOutput;
	
	public ServerHandler(Socket s) throws IOException {
		serverInput = new BufferedReader(new InputStreamReader(s.getInputStream()));
		serverOutput = new PrintWriter(s.getOutputStream(), true);
	}
	
	@Override
	public void run() {
		String s;
		try {
			while (!(s = serverInput.readLine()).equals("exit")) {
				if (s.equals("send login details")) {
					sendLoginDetails();
				} else {
					System.out.println(s);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void sendLoginDetails() throws InterruptedException {
		BlockingQueue<Object> GUIData = new ArrayBlockingQueue<Object>(3);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					LogIn frame = new LogIn(GUIData);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		serverOutput.println("sending login details");
		serverOutput.println((String) GUIData.take());
		serverOutput.println((int) GUIData.take());
		serverOutput.write((char[]) GUIData.take());
		serverOutput.flush();
	}
}
