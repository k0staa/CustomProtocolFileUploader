package com.netDownloader;

import com.netDownloader.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class View extends JFrame  {

	JLabel loginTextArea;
	JPanel buttonPanelA;
	JPanel buttonPanelB;
	JButton getButton;
	JButton pushButton;
	JButton connectButton;
	JButton disconnectButton;
	JTextField connectField;
	private JTextField loginField;
	JButton chooseFolderButton;
	JTextArea logArea;
	//List of Files both from client and server

	JList<DefaultListModel>jListPanelClient;
	JList<DefaultListModel>jListPanelRemote;
	JList<DefaultListModel>jListPanelRemote2;
	JList<DefaultListModel>jListPanelRemote3;

	JScrollPane scrollPanelClient;
	JScrollPane scrollPanelRemote;
	JScrollPane scrollPanelRemote2;
	JScrollPane scrollPanelRemote3;
	JScrollPane scrollPanelLogArea;
	
	JTabbedPane clientPanel;
	JTabbedPane remotePanel;
	JPanel listsPanel;
	JPanel buttonAndLogSouthPanel;

	
	Model model;
	
	
	public View(Model model) {
		this.model = model;

		
		//Button panelA config
		buttonPanelA = new JPanel ();
		getButton = new JButton ("PULL");
		pushButton = new JButton("PUSH");
		chooseFolderButton = new JButton("CHOOSE FOLDER");
		chooseFolderButton.setEnabled(false);
		buttonPanelA.add(getButton);
		buttonPanelA.add(pushButton);
		buttonPanelA.add(chooseFolderButton);
		//Button panelB config
				buttonPanelB = new JPanel ();
				connectButton = new JButton("CONNECT");
				disconnectButton = new JButton("DISCONNECT");
				connectField = new JTextField();
				connectField.setColumns(15);
				connectField.setText("127.0.0.1");
				loginTextArea = new JLabel("Login number: ");
				setLoginField(new JTextField());
				getLoginField().setColumns(5);
				getLoginField().setText("2");
				
				
				buttonPanelB.add(connectField);
				buttonPanelB.add(connectButton);
				buttonPanelB.add(disconnectButton);
				buttonPanelB.add(loginTextArea);
				buttonPanelB.add(getLoginField());
				
		//DefaultListModel Config and init
		
		jListPanelClient = new <DefaultListModel>JList(model.getListModelClient());
		scrollPanelClient = new JScrollPane(jListPanelClient){
				public Dimension getPreferredSize() {
				return new Dimension(350, 400);
												};
			};
		
		//tree  remote hosts 
		
		jListPanelRemote = new <DefaultListModel>JList(model.getRemoteStringLists(0));
		scrollPanelRemote = new JScrollPane(jListPanelRemote) {
			   									public Dimension getPreferredSize() {
			   									return new Dimension(350, 400);
			   																	};
												};
		
		jListPanelRemote2 = new <DefaultListModel>JList(model.getRemoteStringLists(1));
		scrollPanelRemote2 = new JScrollPane(jListPanelRemote2) {
													   		public Dimension getPreferredSize() {
													   		return new Dimension(350, 400);
													   				};
												};
		
		jListPanelRemote3 = new <DefaultListModel>JList(model.getRemoteStringLists(2));
		scrollPanelRemote3 = new JScrollPane(jListPanelRemote3) {
		public Dimension getPreferredSize() {
		return new Dimension(250, 400);
											};
									};
		//Log area
		logArea = new JTextArea();
		logArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPanelLogArea = new JScrollPane(logArea){
								public Dimension getPreferredSize() {
											return new Dimension(500, 100);
								};
					};
		
		
		remotePanel = new JTabbedPane();
		remotePanel.addTab("Remote1", scrollPanelRemote);
		remotePanel.addTab("Remote2", scrollPanelRemote2);
		remotePanel.addTab("Remote3", scrollPanelRemote3);
		
		clientPanel = new JTabbedPane();
		clientPanel.addTab("Client", scrollPanelClient);
		
		
		listsPanel = new JPanel();
		listsPanel.add(clientPanel);	
		listsPanel.add(remotePanel);
		
		buttonAndLogSouthPanel = new JPanel(new BorderLayout());
		buttonAndLogSouthPanel.add(buttonPanelB,BorderLayout.NORTH);
		buttonAndLogSouthPanel.add(scrollPanelLogArea, BorderLayout.SOUTH);
		
		
	}
	ArrayList getJList(){
		ArrayList <JList>list = new ArrayList<JList>();
		
		 list.add(jListPanelRemote);
		 list.add(jListPanelRemote2);
		 list.add(jListPanelRemote3);
		
			
		return list;
		
		
	}
	JComponent getListComponent(){
		
		return listsPanel;
	}
	JComponent getButtonComponentA(){
			
		return buttonPanelA;
	}
	JComponent getButtonComponentB(){
		
		return buttonAndLogSouthPanel;
	}
	String getServerAddres(){
		return connectField.getText();
	}
	void setLog(String s){
		this.logArea.append(s);
		this.logArea.append("\n");
	}
	JList getClientJList(){
		return jListPanelClient;
	}
	public void clearView(){

		
		this.jListPanelRemote.setModel(new DefaultListModel());
		this.jListPanelRemote2.setModel(new DefaultListModel());
		this.jListPanelRemote3.setModel(new DefaultListModel());
	}
	public JTextField getLoginField() {
		return loginField;
	}
	public void setLoginField(JTextField loginField) {
		this.loginField = loginField;
	}

}