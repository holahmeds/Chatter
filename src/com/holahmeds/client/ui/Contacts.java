package com.holahmeds.client.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class Contacts extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultListModel<String> listModelOnline;
	private DefaultListModel<String> listModelOffline;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Contacts frame = new Contacts();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Contacts() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 250, 400);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JLabel lblOnline = new JLabel("Online");
		sl_panel.putConstraint(SpringLayout.NORTH, lblOnline, 10, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblOnline, 10, SpringLayout.WEST, panel);
		panel.add(lblOnline);

		listModelOnline = new DefaultListModel<String>();
		JList<String> listOnline = new JList<String>(listModelOnline);
		sl_panel.putConstraint(SpringLayout.SOUTH, listOnline, 100, SpringLayout.SOUTH, lblOnline);
		listOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sl_panel.putConstraint(SpringLayout.NORTH, listOnline, 10, SpringLayout.SOUTH, lblOnline);
		sl_panel.putConstraint(SpringLayout.WEST, listOnline, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, listOnline, -10, SpringLayout.EAST, panel);
		panel.add(listOnline);
		
		JLabel lblOffliine = new JLabel("Offliine");
		sl_panel.putConstraint(SpringLayout.NORTH, lblOffliine, 10, SpringLayout.SOUTH, listOnline);
		sl_panel.putConstraint(SpringLayout.WEST, lblOffliine, 10, SpringLayout.WEST, panel);
		panel.add(lblOffliine);
		
		listModelOffline = new DefaultListModel<String>();
		JList<String> listOffline = new JList<String>(listModelOffline);
		listOffline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sl_panel.putConstraint(SpringLayout.NORTH, listOffline, 10, SpringLayout.SOUTH, lblOffliine);
		sl_panel.putConstraint(SpringLayout.WEST, listOffline, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, listOffline, -10, SpringLayout.SOUTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, listOffline, -10, SpringLayout.EAST, panel);
		panel.add(listOffline);
		
	}

	public void updateContacts(ArrayList<String> contactDetails) {
		for (String s : contactDetails) {
			String[] sd = s.split(":");
			
			if (sd.length == 2) {
				// contact is online
				if (!listModelOnline.contains(sd[0])) {
					listModelOffline.removeElement(sd[0]);
					listModelOnline.addElement(sd[0]);
				}
			} else {
				// contact offline
				if (!listModelOffline.contains(sd[0])) {
					listModelOnline.removeElement(sd[0]);
					listModelOffline.addElement(sd[0]);
				}
			}
		}
	}
}
