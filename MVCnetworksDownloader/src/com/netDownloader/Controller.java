package com.netDownloader;

import com.netDownloader.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Controller implements ActionListener, ListSelectionListener,
		MouseListener {

	private Model model;
	private View view;
	private WaitingThread waitingThread; // Thread that waits for contact from
											// server
	OutputStream outStream;
	InputStream inStream;
	ObjectOutputStream objOutStream;
	ObjectInputStream objInStream;

	DataInputStream dIn;
	DataOutputStream dOut;
	String currentlySelectedFile; // file selected on remote list
	String currentlySelectedFilePush;
	
	int selectedHost; //host selected for push/pull
	boolean connected = false;
	boolean fileListUploaded = false;

	public Controller(Model tempModel, View tempZad) {
		this.model = tempModel;
		this.view = tempZad;
		this.attachThisListener();
		

	}

	private void attachThisListener() {
		view.chooseFolderButton.addActionListener(this);
		view.getButton.addActionListener(this);
		view.pushButton.addActionListener(this);
		view.connectButton.addActionListener(this);
		view.disconnectButton.addActionListener(this);
		view.jListPanelClient.addListSelectionListener(this);
		view.jListPanelClient
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		view.jListPanelRemote.addListSelectionListener(this);
		view.jListPanelRemote
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		view.jListPanelRemote2.addListSelectionListener(this);
		view.jListPanelRemote2
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		view.jListPanelRemote3.addListSelectionListener(this);
		view.jListPanelRemote3
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		view.remotePanel.addMouseListener(this);
		currentlySelectedFile = null;
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if (action.getSource().equals(view.chooseFolderButton)) {
			if (waitingThread != null) {
				waitingThread.awaitsServer = false;
			}
			JFileChooser chooser = new JFileChooser();

			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				view.setLog("You chose your download/upload folder: "
						+ chooser.getSelectedFile().getName());
				
				try {
					model.setClientFilesHashMap(chooser.getSelectedFile());
				} catch (Exception e1) {
					view.setLog("Problem to get info about files!");
					e1.printStackTrace();
				} 

				if (connected) {
					try {

						// sending message to exchange file list//

						dOut.writeInt(111);
						view.setLog("SEND 111: PUSH FILE LIST ");

						int tempMessage = dIn.readInt();

						switch (tempMessage) {
						case 112:
							view.setLog(("GET 112: PUSHING FILE LIST OK"));
							try {
								objOutStream = new ObjectOutputStream(outStream);
								objOutStream.writeObject(model
										.getFilesAndMD5clientHashMap());
							} finally {
								dOut.writeInt(1);
								view.setLog(("SEND 1: PUSHING FILE LIST OK"));
								this.fileListUploaded = true;
								//this.waitingThread.start(); ADDED
								this.waitingThread.awaitsServer = true;

							}

							break;
						case 110:
							view.setLog(("GET 110: ERROR WHEN PUSHING FILE LIST"));

							break;

						}

					} catch (IOException e) {

						e.printStackTrace();
						view.setLog("Sending message problem\n");
					}

				}

			}
		}
		
		//TO-DO UPLOAD!!
		if (action.getSource().equals(view.pushButton)) {
			if (connected & currentlySelectedFilePush != null) {
				ServerSocket tempServSocket = null;
				int message = 0;
					try{
					waitingThread.awaitsServer = false;		//Stop thread listening server
					if (waitingThread.awaitsServer == false) {
						dOut.writeInt(441);
						view.setLog("SEND 441: PUSH FILE" + "TO HOST: "+ whatRemoteChosen());
						dOut.writeUTF(currentlySelectedFilePush);
						dOut.writeInt( whatRemoteChosen());
						dOut.writeInt(10100);

						message = dIn.readInt();
						
						if(message == 1){
					    tempServSocket = new ServerSocket(10100);

						while (!tempServSocket.isBound()){
							Thread.sleep(100);
						}
							
							DataOutputStream tempDout = new DataOutputStream(tempServSocket.accept().getOutputStream());
							
					        File myFile = model.getFile(currentlySelectedFilePush);
					        view.setLog("SEND 442: START PUSHING FILE");
					        tempDout.writeInt(442);	
					        
							tempDout.writeInt((int) myFile.length());
					          
			                int count;
			                byte[] buffer = new byte[1024];
			                int len=(int) myFile.length();
			                byte[] mybytearray = new byte[len];     
			                
			                tempDout.writeInt(len);
			                BufferedInputStream bis=new BufferedInputStream(new FileInputStream(myFile));
			                bis.read(mybytearray, 0, len);
			                tempDout.write(mybytearray, 0, len);
			                bis.close();
			               
			              
			                tempDout.writeInt(1);
			                view.setLog("SEND 1: PUSHING FILE COMPLETED SUCESFULL");
			                tempDout.close();
							tempServSocket.close();
							
							
							
							
						}else view.setLog("GET 0: CONNECTION PROBLEM OR REMOTE HOST OFFLINE");
						
					}
					
					}catch (Exception e){
						view.setLog("PROBLEM WITH CONNECTION!");
					}finally {
						waitingThread.awaitsServer = true;
					}
			}
		}
		
		
		if (action.getSource().equals(view.getButton)) {
			// Downloading file from remote
			DataOutputStream tempDout = null;
			ServerSocket tempServSocket = null;
			if (connected & currentlySelectedFile != null) {
				int message = 0;
				try {
					waitingThread.awaitsServer = false;		//Stop thread listening server
					if (waitingThread.awaitsServer == false) {
						dOut.writeInt(331);
						view.setLog("SEND 331: PULL FILE FROM HOST: "+ whatRemoteChosen());
						dOut.writeUTF(currentlySelectedFile);
						dOut.writeInt( whatRemoteChosen());
						dOut.writeInt(10100);
						message = dIn.readInt();
						
						if(message == 1){
					    tempServSocket = new ServerSocket(10100);

						while (!tempServSocket.isBound()){
							Thread.sleep(100);
						}
						
						DataInputStream tempDin = new DataInputStream(tempServSocket.accept().getInputStream());
						 
						message = tempDin.readInt();
						
						if(message == 332){
						view.setLog("GET 332: START PULLING");
						int filesize = tempDin.readInt();//12022386;
					
						int bytesRead;
						int currentTot = 0;
						byte[] bytearray = new byte[filesize];
						int len = tempDin.readInt();
						FileOutputStream fos = new FileOutputStream(currentlySelectedFile);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bytesRead = tempDin.read(bytearray, 0, bytearray.length);

						currentTot = bytesRead;
						do {
							bytesRead = tempDin.read(bytearray, currentTot,
									(len - currentTot));
							if (bytesRead >= 0)
								currentTot += bytesRead;

						} while (currentTot < len);
						bos.write(bytearray, 0, currentTot);
						bos.close();
						
						}

						
						message = tempDin.readInt();
						if (message == 1){
							view.setLog("GET 1: PULLING FILE COMPLETE SUCESFULL");	
							tempDin.close();
							tempServSocket.close();
						}


					}else {
						view.setLog("GET 0: PULLING FILE FAILED");	
						
					}

					}
					}catch (IOException e) {
					view.setLog("CONNECTION PROBLEM");
					System.out.println(e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {

					waitingThread.awaitsServer = true;
				}
				
				
			}

		}
		if (action.getSource().equals(view.connectButton)) {

			boolean loginConfirm = true;
			try {
				model.setSocket(new Socket(view.getServerAddres(), 10500));
				if (model.getSocket().isConnected()) {
					
					this.outStream = model.getOutputStream();
					this.inStream = model.getInputStream();
					this.dIn = new DataInputStream(inStream);
					this.dOut = new DataOutputStream(outStream);
					this.dOut.writeUTF(view.getLoginField().getText());
					int message = this.dIn.readInt();
					if(message == 0){
						view.setLog("CONNECTION PROBLEM");
						this.connected = false;
						this.dIn.close();
						this.dOut.close();
						this.model.getSocket().close();
						loginConfirm = false;
					}
					if (loginConfirm){
						this.connected = true;
						this.view.setLog((dIn.readUTF() + "\n"));					
					
						this.model.setClientNumber(Integer.valueOf(view.getLoginField().getText()));
						this.waitingThread = new WaitingThread();
						this.waitingThread.start(); //ADDED
						this.waitingThread.awaitsServer = true;
						view.setLog("Connected!");
						view.chooseFolderButton.setEnabled(true);
						view.connectButton.setEnabled(false);
					}
					
				}
			} catch (IOException e) {
			
				view.setLog("CONNECTION PROBLEM");
				System.out.println(e);
			}
			
		}
		if (action.getSource().equals(view.disconnectButton)) {
			while (this.connected){
			try {

				dOut.writeInt(999);
				view.setLog("SEND 999: DISCONNECT");
				waitingThread.awaitsServer = false;
				dOut.close();
				dIn.close();
				model.closeSocket();
				model.clearModel();
				view.clearView();
				view.connectButton.setEnabled(true);
				this.connected = false;

			} catch (IOException e) {
				view.setLog("CONNECTION PROBLEM");
				System.out.println(e);
			}

		}
		}

	}

	@Override
	public void valueChanged(ListSelectionEvent selectEvent) {

		if (selectEvent.getSource().equals(view.jListPanelClient)) {
			JList jListTemp = (JList) selectEvent.getSource();
			if (!selectEvent.getValueIsAdjusting()) {
				if (model.getListModelClient().size() > 0
						& jListTemp.getSelectedIndex() >= 0) {
					view.setLog("File chosen: "
							+ model.getListModelClient().get(
									jListTemp.getSelectedIndex()) + "\n");
					currentlySelectedFilePush = model.getListModelClient().get(
							jListTemp.getSelectedIndex());
				}

			}

		}
		if (selectEvent.getSource().equals(view.jListPanelRemote)) {
			JList jListTemp = (JList) selectEvent.getSource();
		
			if (!selectEvent.getValueIsAdjusting()) {
				if (model.getRemoteStringLists(0).size() > 0
						& jListTemp.getSelectedIndex() >= 0) {
					view.setLog("Remote file chosen: "
							+ model.getRemoteStringLists(0).get(
									jListTemp.getSelectedIndex()) + "\n");
					currentlySelectedFile = model.getRemoteStringLists(0).get(
							jListTemp.getSelectedIndex());
					view.setLog("Remote file hash: "
							+ model.getRemoteHash(currentlySelectedFile, 0)
							+ "\n");
					
				}

			}
		}
		if (selectEvent.getSource().equals(view.jListPanelRemote2)) {
			JList jListTemp = (JList) selectEvent.getSource();
			
			if (!selectEvent.getValueIsAdjusting()) {

				if (model.getRemoteStringLists(1).size() > 0
						& jListTemp.getSelectedIndex() >= 0) {
					view.setLog("Remote file chosen: "
							+ model.getRemoteStringLists(1).get(
									jListTemp.getSelectedIndex()) + "\n");
					currentlySelectedFile = model.getRemoteStringLists(1).get(
							jListTemp.getSelectedIndex());
					view.setLog("Remote file hash: "
							+ model.getRemoteHash(currentlySelectedFile, 1)
							+ "\n");
				}

			}
		}
		if (selectEvent.getSource().equals(view.jListPanelRemote3)) {
			JList jListTemp = (JList) selectEvent.getSource();
			
			if (!selectEvent.getValueIsAdjusting()) {
				if (model.getRemoteStringLists(2).size() > 0
						& jListTemp.getSelectedIndex() >= 0) {
					view.setLog("Remote file chosen: "
							+ model.getRemoteStringLists(2).get(
									jListTemp.getSelectedIndex()) + "\n");
					currentlySelectedFile = model.getRemoteStringLists(2).get(
							jListTemp.getSelectedIndex());
					view.setLog("Remote file hash: "
							+ model.getRemoteHash(currentlySelectedFile, 2)
							+ "\n");
				}

			}
		}

	}
public int whatRemoteChosen(){
	int selected = 0;
	for (int i = 0 ; i < 3 ; i++){
			
			if (view.remotePanel.getComponentAt(i).isShowing() ){
				selected = i;
			}
	
	
	
	}
	view.setLog("HOST SELECTED FOR PUSH/PULL: " + selected);
	
	return selected;
	
}
	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getSource().equals(view.remotePanel)) {
			selectedHost = whatRemoteChosen();
			currentlySelectedFilePush = null;
			currentlySelectedFile = null;
			if (connected) {
				try {
					waitingThread.awaitsServer = false;

					dOut.writeInt(221);
					view.setLog("SEND 221: PULL FILE LIST FROM SERVER");
					int tempMessage = dIn.readInt();
					if (tempMessage == 222) {
						view.setLog("GET 222: PULL FILE LIST FROM SERVER OK");
						try {

							objInStream = new ObjectInputStream(inStream);

							model.setModelRemoteMaps(
									(HashMap<Integer, HashMap<Byte[], String>>) objInStream
											.readObject(), view.getJList(),view);

						} catch (ClassNotFoundException e) {
							view.setLog("Sended object class is wrong try again!");
							e.printStackTrace();

						} finally {

							if (dIn.readInt() == 1) {
								view.setLog("GET 1: PULL FILE LIST OK");

								waitingThread.awaitsServer = true;

							} else {
								view.setLog("GET 0: PULL FILE LIST FAILED");
							}

						}
					}

				} catch (IOException e) {
					view.setLog("CONNECTION PROBLEM");
					System.out.println(e);
				}
			}
		}

	}
	
	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	// Thread used for wainting for server response when downloading and
	// uploading files//
	private class WaitingThread extends Thread {
		volatile boolean threadSuspended = false;
		volatile boolean awaitsServer = false;
		private String  filenameToDownload = "default";
		 String addres = null;
         int port;
         Socket tempSock = null;
         DataOutputStream tempDout;

		public void run() {
			while (connected) {
				int tempMessage = 0;

				
					try {
						sleep(1000);
					


					if (awaitsServer == true) {

						if (dIn.available() ==0) {
							//view.setLog("."); FOR TESTING ONLY
							


						} else {
							tempMessage = dIn.readInt();

							switch (tempMessage) {
							// TO DO ALL KIND OF COMMUNICATION FROM AND TO
							
								//Sending file to server
									case 331:	filenameToDownload = dIn.readUTF();
												view.setLog("GET 331: REQUEST FOR PULLING FILE");
												 // send file
										          File myFile = model.getFile(filenameToDownload);
										          //Adding below
										          addres = dIn.readUTF();
										          port = dIn.readInt();
										          tempSock = new Socket(addres,port);
										          DataOutputStream tempDout = new DataOutputStream(tempSock.getOutputStream());
									          view.setLog("SEND 332: START PULLING FILE");
									          tempDout.writeInt(332);	
									          tempDout.writeInt((int) myFile.length());
										          
									                int count;
									                byte[] buffer = new byte[1024];
									                int len=(int) myFile.length();
									                byte[] mybytearray = new byte[len];     
									                
									                tempDout.writeInt(len);
									                BufferedInputStream bis=new BufferedInputStream(new FileInputStream(myFile));
									                bis.read(mybytearray, 0, len);
									                tempDout.write(mybytearray, 0, len);
									                bis.close();
									               
									              
									                tempDout.writeInt(1);
									                view.setLog("SEND 1: PULLING FILE COMPLETE SUCESFULL");
									                tempDout.flush();
									                tempDout.close();
									                tempSock.close();
										         
										          awaitsServer = true;
										          
										          break;
								//Downloading file when PUSHED to server	TO-DO!
									case 441:									
											filenameToDownload = dIn.readUTF();
											addres = dIn.readUTF();
									        port = dIn.readInt();
											view.setLog("GET 441: REQUEST FOR PUSHING FILE");
											tempSock = new Socket(addres,port);											
											
											tempDout = new DataOutputStream(tempSock.getOutputStream());
											DataInputStream tempDin = new DataInputStream(tempSock.getInputStream());
											
											if (tempDin.readInt() == 442){
											view.setLog("GET 442: START PUSHING FILE");
									        	
											int filesize = tempDin.readInt();//12022386;
											
											int bytesRead;
											int currentTot = 0;
											byte[] bytearray = new byte[filesize];
											int lent = tempDin.readInt();
											FileOutputStream fos = new FileOutputStream(filenameToDownload);
											BufferedOutputStream bos = new BufferedOutputStream(fos);
											bytesRead = tempDin.read(bytearray, 0, bytearray.length);

											currentTot = bytesRead;
											do {
												bytesRead = tempDin.read(bytearray, currentTot,
														(lent - currentTot));
												if (bytesRead >= 0)
													currentTot += bytesRead;

											} while (currentTot < lent);
											bos.write(bytearray, 0, currentTot);
											bos.close();
											
											

											
											int message = tempDin.readInt();
											if (message == 1){
												view.setLog("GET 1: PUSHING FILE COMPLETED SUCESFULLY");	
												tempDin.close();
								                tempSock.close();
											}
											}else 	view.setLog("GET 440: PUSHING FILE ABORTED");

											
									          awaitsServer = true;

											
									        break;
							}
						}
					}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
						
			}
		}

		
	}
