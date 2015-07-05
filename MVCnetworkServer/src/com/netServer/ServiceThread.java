package com.netServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;

public class ServiceThread extends java.lang.Thread {
	private Socket socket;
	private int clientNumber;
	private ResolveThread resolver;
	HashMap<Byte[], String> clientHashMap;
	private static Integer clientAmount = 0;
	private static String[] userList = {"0","1","2"};
	private static Integer[] currentlyLogged = new Integer[3];
	private static Map<Integer, HashMap<Byte[], String>> remoteModelHashMap = new HashMap<Integer, HashMap<Byte[], String>>();
	InputStream inStream;
	OutputStream outStream;
	DataOutputStream dOut;
	DataInputStream dIn;
	boolean modelUploaded = false;
	boolean loginSucces = false;

	String line;

	boolean connectionOpen = true;

	public ServiceThread(Socket s, ResolveThread mainResolver) {

		this.socket = s;
		this.clientAmount++;
		new DefaultListModel<String>();
		this.clientHashMap = new HashMap<Byte[], String>();
		this.resolver = mainResolver;
		
	
	}

	public void run() {
		try {
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
			dIn = new DataInputStream(inStream);
			dOut = new DataOutputStream(outStream);
			String userLogin = dIn.readUTF();
			System.out.println(userLogin);
			if(Arrays.binarySearch(userList, userLogin) >= 0  & this.clientAmount < 4){
				if(currentlyLogged[Integer.valueOf(userLogin)] == null){
				this.clientNumber = Integer.valueOf(userLogin);
				log("New connection with client# " + clientNumber + " at " + socket);
				currentlyLogged[Integer.valueOf(userLogin)] = Integer.valueOf(userLogin);
				
				resolver.addServiceToList(this,this.clientNumber);
				dOut.writeInt(1); 
				// Send a welcome message to the client.
				dOut.writeUTF("Connection established, you are client #"
						+ clientNumber + ".");
				this.loginSucces = true;
				//dOut.writeInt(clientNumber);
				}else{
					dOut.writeInt(0);
					connectionOpen = false;
					
					System.out.println("User login false or user currently logged. Disconnecting NOW!");
				}
			}else {
				dOut.writeInt(0);
				connectionOpen = false;
				
				System.out.println("User login false or user currently logged. Disconnecting NOW!");
					
				}

			
			while (connectionOpen) {
				int tempMessage = dIn.readInt();
				String name = null;
				int remoteHostNumb;
				int portNumbServ;
				
				switch (tempMessage) {
				case 111:
					//
					log("GET 111: REQUEST FOR PUSHING FILE LIST");
					dOut.writeInt(112);
					log("SEND 112: PUSH LIST OK");
					ObjectInputStream objInStream = null;
					try {

						objInStream = new ObjectInputStream(inStream);
						clientHashMap = (HashMap<Byte[], String>) objInStream
								.readObject();

					} catch (ClassNotFoundException e) {
						log("Sended object class is wrong try again!");
						e.printStackTrace();
						break;
					} finally {

						if (dIn.readInt() == 1) {
							log("GET 1: PUSHING FILE LIST COMPLETED WITH SUCCES");
							modelUploaded = true;
							
								getRemoteModelHashMap().put(this.clientNumber,
										this.clientHashMap);
							

						} else
							log("GET 0: PUSHING FILE LIST FAILED");

					}
					break;
				case 221:
					log("SERVER GET 221: REQUEST FOR PULLING FILE LIST");
					dOut.writeInt(222);
					log("SERVER SEND 222: PULLING FILE LIST OK");
					try {
						ObjectOutputStream objOutStream = new ObjectOutputStream(
								outStream);
						objOutStream.writeObject(this.getRemoteModelHashMap());
					} finally {
						dOut.writeInt(1);
						log(("SEND 1: PULLING FILE LIST COMPLETED WITH SUCCES"));

					}

					break;
					
				// Want to GET file from client of this ServiceThread
				case 331:

					name = dIn.readUTF();
					remoteHostNumb = dIn.readInt();
					portNumbServ = dIn.readInt();		
					
					log("SERVER GET 331: REQUEST FOR PULL FILE " + name + " FROM HOST "
							+ remoteHostNumb);
					if(currentlyLogged[remoteHostNumb] != null){
					
					this.dOut.writeInt(1);
					resolver.setNeedResolve(this, remoteHostNumb,portNumbServ, 331, name); // Sending problem to resolver //
					log("SERVER SEND 1: RESOURCE AVAILABLE ");
					}else {
						this.dOut.writeInt(0);
						log("SERVER SEND 0: RESOURCE NOT AVAILABLE ");
					}
		

					break;
					
					// TO-DO! Want to PUSH file from client of this ServiceThread
				case 441:
					name = dIn.readUTF();
					remoteHostNumb = dIn.readInt();
					portNumbServ = dIn.readInt();	
					
					log("SERVER GET 441: REQUEST FOR PUSH FILE " + name + " TO HOST "
							+ remoteHostNumb);
					
					if(currentlyLogged[remoteHostNumb] != null){
					this.dOut.writeInt(1);
					//Thread.sleep(1000);
					resolver.setNeedResolve(this, remoteHostNumb,portNumbServ, 441, name); // Sending problem to resolver //
					log("SERVER SEND 1: CLIENT AVAILABLE ");
					
					}else {
						this.dOut.writeInt(0);
						log("SERVER SEND 0: CLIENT NOT AVAILABLE ");
					}
					break;
					
				case 999:			
					connectionOpen = false;
					break;

				}
			}

		} catch (Exception e) {
			log("Error handling client# " + clientNumber + ": " + e);
		}  finally {
			try {
				log("GET 999: DISCONNECT");
				if(this.loginSucces){
				resolver.clientDead(this);
				getRemoteModelHashMap().remove(this.clientNumber);
				currentlyLogged[this.clientNumber] = null;
				}
				this.clientAmount--;
				socket.close();

			} catch (IOException e) {
				log("Couldn't close a socket, what's going on?");
			}
			
		}

	}

	private void log(String message) {
		System.out.println(message);
	}

	public int getClientNumber() {
		return this.clientNumber;
	}

	

	public static Map<Integer, HashMap<Byte[], String>> getRemoteModelHashMap() {
		return remoteModelHashMap;
	}

	public static void setRemoteModelHashMap(
			Map<Integer, HashMap<Byte[], String>> remoteModelHashMap) {
		ServiceThread.remoteModelHashMap = remoteModelHashMap;
	}

	public void setInOutStreams() {
		dIn = new DataInputStream(inStream);
		dOut = new DataOutputStream(outStream);

	}

	public Socket getSocket() {
		return socket;
	}

}
