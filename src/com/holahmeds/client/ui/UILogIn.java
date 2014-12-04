package com.holahmeds.client.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class UILogIn extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BlockingQueue<Object> dataOutputQueue;
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JLabel lblChatter;
	private Component verticalStrut_1;
	private JLabel lblUsername;
	private JLabel lblPassword;

	/**
	 * Create the frame.
	 */
	public UILogIn() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 250);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {30, 0, 0, 30};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 2;
		gbc_verticalStrut_1.gridy = 0;
		contentPane.add(verticalStrut_1, gbc_verticalStrut_1);
		
		lblChatter = new JLabel("Chatter");
		lblChatter.setFont(new Font("Dialog", Font.BOLD, 20));
		GridBagConstraints gbc_lblChatter = new GridBagConstraints();
		gbc_lblChatter.gridwidth = 2;
		gbc_lblChatter.insets = new Insets(0, 0, 5, 0);
		gbc_lblChatter.gridx = 1;
		gbc_lblChatter.gridy = 1;
		contentPane.add(lblChatter, gbc_lblChatter);
		
		lblInvalidUsernameOr = new JLabel("Invalid Username or Password");
		lblInvalidUsernameOr.setForeground(Color.RED);
		GridBagConstraints gbc_lblInvalidUsernameOr = new GridBagConstraints();
		gbc_lblInvalidUsernameOr.gridwidth = 2;
		gbc_lblInvalidUsernameOr.insets = new Insets(0, 0, 5, 0);
		gbc_lblInvalidUsernameOr.gridx = 1;
		gbc_lblInvalidUsernameOr.gridy = 2;
		contentPane.add(lblInvalidUsernameOr, gbc_lblInvalidUsernameOr);
		
		lblUsername = new JLabel("Username");
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsername.anchor = GridBagConstraints.EAST;
		gbc_lblUsername.gridx = 1;
		gbc_lblUsername.gridy = 3;
		contentPane.add(lblUsername, gbc_lblUsername);
		
		txtUsername = new JTextField();
		txtUsername.setToolTipText("");
		GridBagConstraints gbc_txtUsername = new GridBagConstraints();
		gbc_txtUsername.insets = new Insets(0, 0, 5, 0);
		gbc_txtUsername.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUsername.gridx = 2;
		gbc_txtUsername.gridy = 3;
		contentPane.add(txtUsername, gbc_txtUsername);
		txtUsername.setColumns(10);
		txtUsername.addKeyListener(enterKeyAdapter);
		
		lblPassword = new JLabel("Password");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.gridx = 1;
		gbc_lblPassword.gridy = 4;
		contentPane.add(lblPassword, gbc_lblPassword);
		
		txtPassword = new JPasswordField();
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.insets = new Insets(0, 0, 5, 0);
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 2;
		gbc_txtPassword.gridy = 4;
		contentPane.add(txtPassword, gbc_txtPassword);
		txtPassword.setColumns(10);
		txtPassword.addKeyListener(enterKeyAdapter);
		
		JButton btnSignIn = new JButton("Sign In");
		btnSignIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sendLoginDetails();
			}
		});
		GridBagConstraints gbc_btnSignIn = new GridBagConstraints();
		gbc_btnSignIn.gridwidth = 2;
		gbc_btnSignIn.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSignIn.gridx = 1;
		gbc_btnSignIn.gridy = 5;
		contentPane.add(btnSignIn, gbc_btnSignIn);
	}
	
	public UILogIn(BlockingQueue<Object> queue) {
		this();
		dataOutputQueue = queue;
	}
	
	private void sendLoginDetails() {
		dataOutputQueue.add(txtUsername.getText());
		dataOutputQueue.add(txtPassword.getPassword().length);
		dataOutputQueue.add(txtPassword.getPassword());
		UILogIn.this.dispose();
	}
	private JLabel lblInvalidUsernameOr;
	
	public void show(boolean retrying, boolean reloggingin) {
		lblInvalidUsernameOr.setVisible(retrying);
		txtUsername.setEditable(!reloggingin);
		txtPassword.setText("");
		
		this.setVisible(true);
	}
	
	KeyAdapter enterKeyAdapter = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				sendLoginDetails();
			}
		}
	};
}
