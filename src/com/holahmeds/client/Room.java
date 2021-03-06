package com.holahmeds.client;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.holahmeds.client.ui.UIRoom;

/**
 * Creates a room window and shows messages sent to and from the room. 
 * @author ahmed
 *
 */
public class Room implements Runnable {
	private String roomID;
	private UIRoom ui;
	private LinkedBlockingQueue<String> updates;
	
	public Room(String rid, LinkedBlockingQueue<String> updateQueue) {
		roomID = rid;
		updates = updateQueue;
		ui = new UIRoom(updates);
	}

	@Override
	public void run() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				ui.setVisible(true);
			}
		});
		
		while (true) {
			try {
				// refresh members in the room.
				ArrayList<String> members = Client.request("send room members\n"
						+ roomID + '\n');
				String[] event = (updates.isEmpty())
						? null
						: updates.take().split(":");
				if (event != null && event[0].equals("close")) {
					Client.request("remove user from room\n" + roomID + '\n');
					break;
				}

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						ui.refreshUsers(members);
						
						if (event == null) {
							return;
						}

						switch (event[0]) {
						case "message":
							ui.appendMessage(event[2] + ":" + event[1]);
							break;
						case "send":
							try {
								Client.request("send room message\n"
										+ roomID + '\n'
										+ "message:" + event[1] + '\n');
							} catch (Exception e) {
							}
							break;
						case "add":
							try {
								Client.request("add user to room\n"
										+ roomID + '\n'
										+ event[1]);
							} catch (Exception e) {
							}
							break;
						default:
							System.out.println("Unhandled Event " + event[0]);
						}
					}
				});
				
				Thread.sleep(500);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
