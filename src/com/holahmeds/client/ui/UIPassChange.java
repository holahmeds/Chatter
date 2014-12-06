package com.holahmeds.client.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.holahmeds.client.Client;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.awt.Component;

import javax.swing.Box;

public class UIPassChange extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8613138128162821148L;
	private JPanel contentPane;
	private JPasswordField pwdOld;
	private JPasswordField pwdNew;
	private JPasswordField pwdConfirmNew;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIPassChange frame = new UIPassChange();
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
	public UIPassChange() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 230);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {30, 0, 0, 30};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.gridwidth = 2;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 1;
		gbc_verticalStrut.gridy = 0;
		contentPane.add(verticalStrut, gbc_verticalStrut);

		JLabel lblOldPassword = new JLabel("Old Password");
		GridBagConstraints gbc_lblOldPassword = new GridBagConstraints();
		gbc_lblOldPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblOldPassword.anchor = GridBagConstraints.WEST;
		gbc_lblOldPassword.gridx = 1;
		gbc_lblOldPassword.gridy = 1;
		contentPane.add(lblOldPassword, gbc_lblOldPassword);

		pwdOld = new JPasswordField();
		GridBagConstraints gbc_pwdOld = new GridBagConstraints();
		gbc_pwdOld.insets = new Insets(0, 0, 5, 0);
		gbc_pwdOld.fill = GridBagConstraints.HORIZONTAL;
		gbc_pwdOld.gridx = 2;
		gbc_pwdOld.gridy = 1;
		contentPane.add(pwdOld, gbc_pwdOld);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.gridwidth = 2;
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 1;
		gbc_verticalStrut_1.gridy = 2;
		contentPane.add(verticalStrut_1, gbc_verticalStrut_1);

		JLabel lblNewPassword = new JLabel("New Password");
		GridBagConstraints gbc_lblNewPassword = new GridBagConstraints();
		gbc_lblNewPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewPassword.anchor = GridBagConstraints.WEST;
		gbc_lblNewPassword.gridx = 1;
		gbc_lblNewPassword.gridy = 3;
		contentPane.add(lblNewPassword, gbc_lblNewPassword);

		pwdNew = new JPasswordField();
		GridBagConstraints gbc_pwdNew = new GridBagConstraints();
		gbc_pwdNew.insets = new Insets(0, 0, 5, 0);
		gbc_pwdNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_pwdNew.gridx = 2;
		gbc_pwdNew.gridy = 3;
		contentPane.add(pwdNew, gbc_pwdNew);

		JLabel lblConfirmNewPassword = new JLabel("Confirm New");
		GridBagConstraints gbc_lblConfirmNewPassword = new GridBagConstraints();
		gbc_lblConfirmNewPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblConfirmNewPassword.anchor = GridBagConstraints.WEST;
		gbc_lblConfirmNewPassword.gridx = 1;
		gbc_lblConfirmNewPassword.gridy = 4;
		contentPane.add(lblConfirmNewPassword, gbc_lblConfirmNewPassword);

		pwdConfirmNew = new JPasswordField();
		GridBagConstraints gbc_pwdConfirmNew = new GridBagConstraints();
		gbc_pwdConfirmNew.insets = new Insets(0, 0, 5, 0);
		gbc_pwdConfirmNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_pwdConfirmNew.gridx = 2;
		gbc_pwdConfirmNew.gridy = 4;
		contentPane.add(pwdConfirmNew, gbc_pwdConfirmNew);

		JButton btnChange = new JButton("Change");
		btnChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (Client.changePass(
							pwdOld.getPassword(),
							pwdConfirmNew.getPassword())) {
						
						JOptionPane.showMessageDialog(UIPassChange.this,
								"Password Changed Successfully");
						UIPassChange.this.dispose();
					} else {
						JOptionPane.showMessageDialog(UIPassChange.this,
								"Old Password Incorrect");
					}
				} catch (IOException e1) {
				}
			}
		});
		btnChange.setEnabled(false);
		GridBagConstraints gbc_btnChange = new GridBagConstraints();
		gbc_btnChange.gridwidth = 2;
		gbc_btnChange.gridx = 1;
		gbc_btnChange.gridy = 6;
		contentPane.add(btnChange, gbc_btnChange);

		DocumentListener confirmNewPass = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				enableButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				enableButton();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				enableButton();
			}

			public void enableButton() {
				btnChange.setEnabled(
						pwdNew.getPassword().length != 0
						&& Arrays.equals(pwdNew.getPassword(),
								pwdConfirmNew.getPassword()));
			}
		};
		pwdNew.getDocument().addDocumentListener(confirmNewPass);
		pwdConfirmNew.getDocument().addDocumentListener(confirmNewPass);
	}

}
