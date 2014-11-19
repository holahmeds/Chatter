package com.holahmeds.client.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class Contacts extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultTreeModel root;
	private DefaultMutableTreeNode onlineContacts;
	private DefaultMutableTreeNode offlineContacts;
	private HashMap<String, DefaultMutableTreeNode> contacts;

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
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTree tree = new JTree();
		tree.setRootVisible(false);
		tree.setEditable(true);
		tree.setModel(root = new DefaultTreeModel(
			new DefaultMutableTreeNode("JTree") {
				private static final long serialVersionUID = -4562470831848585839L;

				{
					onlineContacts = new DefaultMutableTreeNode("Online");
					add(onlineContacts);
					offlineContacts = new DefaultMutableTreeNode("Offline");
					add(offlineContacts);
				}
			}
		));
		contentPane.add(tree, BorderLayout.CENTER);
		
		contacts = new HashMap<String, DefaultMutableTreeNode>();
	}

	public void updateContacts(ArrayList<String> contactDetails) {
		for (String s : contactDetails) {
			String[] sd = s.split(":");
			
			DefaultMutableTreeNode stn = null;
			if (!contacts.containsKey(sd[0])) {
				stn = new DefaultMutableTreeNode(sd[0]);
				contacts.put(sd[0], stn);
			} else {
				stn = contacts.get(sd[0]);
			}
			
			if (sd.length == 2) {
				// contact is online
				if (!onlineContacts.isNodeChild(stn)) {
					root.removeNodeFromParent(stn);
					root.insertNodeInto(stn, onlineContacts, onlineContacts.getChildCount());
				}
			} else {
				// contact offline
				if (!offlineContacts.isNodeChild(stn)) {
					root.removeNodeFromParent(stn);
					root.insertNodeInto(stn, offlineContacts, offlineContacts.getChildCount());
				}
			}
		}
	}
}
