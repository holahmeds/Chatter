package com.holahmeds.client.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.holahmeds.client.Client;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class UIRoom extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private DefaultListModel<String> listModel;
	private JTextPane textPane;
	private StringBuilder messages;
	private LinkedBlockingQueue<String> dataOutputQueue;
	private JMenuBar menuBar;
	private JMenu mnRoom;
	private JMenuItem mntmAddContact;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIRoom frame = new UIRoom(null);
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
	public UIRoom(LinkedBlockingQueue<String> queue) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 5));
		setContentPane(contentPane);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				dataOutputQueue.add("close");
			}
		});
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		contentPane.add(textPane, BorderLayout.CENTER);
		
		textField = new JTextField();
		contentPane.add(textField, BorderLayout.SOUTH);
		textField.setColumns(10);
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !textField.getText().equals("")) {
					dataOutputQueue.add("send:" + textField.getText());
					textField.setText("");
				}
			}
		});
		
		JList<String> list = new JList<String>(listModel = new DefaultListModel<String>());
		contentPane.add(list, BorderLayout.EAST);
		
		menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		mnRoom = new JMenu("Room");
		menuBar.add(mnRoom);
		
		mntmAddContact = new JMenuItem("Add  contact");
		mntmAddContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String toAdd = (String) JOptionPane.showInputDialog(
						UIRoom.this, "Select contact to add:", null,
						JOptionPane.PLAIN_MESSAGE, null,
						Client.getOnlineContacts(), null);
				
				dataOutputQueue.add("add:" + toAdd);
			}
		});
		mnRoom.add(mntmAddContact);
		
		messages = new StringBuilder();
		dataOutputQueue = queue;
	}

	public void refreshUsers(ArrayList<String> users) {
		listModel.removeAllElements();
		for (String s : users) {
			if (!listModel.contains(s)) {
				listModel.addElement(s);
			}
		}
		for (Object s : listModel.toArray()) {
			if (!users.contains((String) s)) {
				listModel.removeElement(s);
			}
		}
	}
	
	public void appendMessage(String message) {
		messages.append(message).append('\n');
		textPane.setText(messages.toString());
	}
}
