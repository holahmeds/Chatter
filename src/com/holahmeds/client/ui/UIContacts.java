package com.holahmeds.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.holahmeds.client.Client;

public class UIContacts extends JFrame {

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
					UIContacts frame = new UIContacts();
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
	public UIContacts() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 250, 400);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		scrollPane.setViewportView(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);

		JLabel lblOnline = new JLabel("Online");
		sl_panel.putConstraint(SpringLayout.NORTH, lblOnline, 10, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblOnline, 10, SpringLayout.WEST, panel);
		panel.add(lblOnline);

		listModelOnline = new DefaultListModel<String>();
		JList<String> listOnline = new JList<String>(listModelOnline);
		listOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sl_panel.putConstraint(SpringLayout.NORTH, listOnline, 10, SpringLayout.SOUTH, lblOnline);
		sl_panel.putConstraint(SpringLayout.WEST, listOnline, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, listOnline, -10, SpringLayout.EAST, panel);
		panel.add(listOnline);
		listOnline.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					try {
						Client.request("create room\n" + listOnline.getSelectedValue() + '\n');
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		JLabel lblOffliine = new JLabel("Offliine");
		sl_panel.putConstraint(SpringLayout.NORTH, lblOffliine, 10, SpringLayout.SOUTH, listOnline);
		sl_panel.putConstraint(SpringLayout.WEST, lblOffliine, 10, SpringLayout.WEST, panel);
		panel.add(lblOffliine);

		listModelOffline = new DefaultListModel<String>();
		JList<String> listOffline = new JList<String>(listModelOffline);
		listOffline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sl_panel.putConstraint(SpringLayout.NORTH, listOffline, 10, SpringLayout.SOUTH, lblOffliine);
		sl_panel.putConstraint(SpringLayout.WEST, listOffline, 10, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, listOffline, -10, SpringLayout.EAST, panel);
		panel.add(listOffline);

		listOnline.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				listOffline.clearSelection();
			}
		});
		listOffline.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				listOnline.clearSelection();
			}
		});


		JMenuBar menuBar = new JMenuBar();
		getContentPane().add(menuBar, BorderLayout.NORTH);

		JMenu mnContacts = new JMenu("Contacts");
		menuBar.add(mnContacts);

		JMenuItem mntmAddContact = new JMenuItem("Add Contact");
		mnContacts.add(mntmAddContact);
		mntmAddContact.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Client.request("add contact\n"
							+ JOptionPane.showInputDialog("Enter Usename:")
							+ '\n');
				} catch (HeadlessException | IOException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});

		JPopupMenu contactContext = new JPopupMenu();

		JMenuItem mntmRemoveContact = new JMenuItem("Remove Contact");
		contactContext.add(mntmRemoveContact);
		mntmRemoveContact.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String toRemove = null;
				if (!listOffline.isSelectionEmpty()) {
					toRemove = listOffline.getSelectedValue();
				} else if (!listOnline.isSelectionEmpty()) {
					toRemove = listOnline.getSelectedValue();
				}

				try {
					Client.request("remove contact\n" + toRemove + '\n');
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});

		MouseAdapter contactContextOpener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					contactContext.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		listOnline.addMouseListener(contactContextOpener);
		listOffline.addMouseListener(contactContextOpener);
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
		
		for (Object s : listModelOffline.toArray()) {
			if (!contactDetails.contains(s)) {
				listModelOffline.removeElement(s);
			}
		}
		for (Object s : listModelOnline.toArray()) {
			if (!contactDetails.contains(s + ":o")) {
				listModelOnline.removeElement(s);
			}
		}
	}
}
