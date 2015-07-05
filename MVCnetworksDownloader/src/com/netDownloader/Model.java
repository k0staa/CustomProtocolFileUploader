package com.netDownloader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.filechooser.FileFilter;

import com.netDownloader.Helpers.MD5Checksum;

class Model {
	int clientNumber;
	String[] filesNamesTab;	
	Socket myClient;					//main socket
	File[] listOfFiles;
	ArrayList<DefaultListModel> remoteModelLists;
	Map<Integer, HashMap<Byte[], String>> remoteModelMaps;

	Map<Byte[], String> filesAndMD5clientHashMap;
	DefaultListModel<String> listModelClient;


	public Model() {

		this.remoteModelLists = new ArrayList<DefaultListModel>();
		this.remoteModelMaps = new HashMap<Integer, HashMap<Byte[], String>>();
		this.filesAndMD5clientHashMap = new HashMap<Byte[], String>();
		this.listModelClient = new DefaultListModel<String>();
		for (int x = 0; x < 3; x++) {
			this.remoteModelLists.add(new DefaultListModel<String>());
		}

	}

	public DefaultListModel<String> getListModelClient() {
		return listModelClient;
	}

	public void setListModel(String[] tempStringTab,
			DefaultListModel<String> list) {

		list.removeAllElements();

		for (int i = 0; i < tempStringTab.length; i++) {

			list.add(i, tempStringTab[i]);
		}

	}

	public DefaultListModel<String> getRemoteStringLists(int index) {
		return remoteModelLists.get(index);
	}

	public String getRemoteHash(String fileName, int indexRemoteList) {
		String result = "";
		HashMap<Byte[], String> tempMap = remoteModelMaps.get(indexRemoteList);
		Iterator<?> it = tempMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (pair.getValue().equals(fileName)) {
				 
					Byte[] b = (Byte[])pair.getKey();
			       for (int i=0; i < b.length; i++) {
			           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
			       }
			       
			}

		}
		return result;

		

	}

	public void setModelRemoteMaps(
			HashMap<Integer, HashMap<Byte[], String>> remoteMap,
			ArrayList<JList> listFromView, View v) {
		v.clearView();
		this.remoteModelLists = new ArrayList<DefaultListModel>();
		this.remoteModelMaps = new HashMap<Integer, HashMap<Byte[], String>>();
		for (int x = 0; x < 3; x++) {
			this.remoteModelLists.add(new DefaultListModel<String>());
		}
		// First adding remote hashmap that doesnt exist in client maps
		for (Map.Entry<Integer, HashMap<Byte[], String>> entry : remoteMap
				.entrySet()) {
			if (!remoteModelMaps.containsKey(entry.getKey())
					& entry.getKey() != this.clientNumber) {
				remoteModelMaps.put(entry.getKey(), entry.getValue());
			}
		}
		// Now making DefaultListModel from remote HashMap
		for (Map.Entry<Integer, HashMap<Byte[], String>> entry : remoteModelMaps
				.entrySet()) {
			Map<Byte[], String> tempMap = entry.getValue();
			int x = 0;
			String[] temp = new String[tempMap.size()];
			Iterator<?> it = tempMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				String strtemp = "" + pair.getValue();
				temp[x] = strtemp;
				x++;
			}

			setListModel(temp, remoteModelLists.get(entry.getKey()));

			listFromView.get(entry.getKey()).setModel(
					remoteModelLists.get(entry.getKey()));

		}

	}

	public void setClientFilesHashMap(File folder) throws Exception {
		// We need to check how meny files are in directory and initialize
		// listOfFiles with this size and then with file names. We will then
		// get MD5 hash from all files and put them in array
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.out
					.println("Problem to load proper hashing algorithm from system!");
			e.printStackTrace();
		} // for MD5
		File[] listF = folder.listFiles();

		int x = 0;
		for (File file : listF) {
			if (file.isDirectory() == false) {

				x++;
			}
		}
		listOfFiles = new File[x];
		x = 0;
		for (File file : listF) {
			if (file.isDirectory() == false) {
				listOfFiles[x] = file;
				x++;
			}
		}

		// now we returning file names in string tab and all the magic with MD5
		// checksum
		this.listModelClient.clear();
		this.filesAndMD5clientHashMap.clear();
		for (int i = 0; i < listOfFiles.length; i++) {
			

			
			// System.out.println("MD5 "+md5Array[i]); //TEST shows MD5 from
			// files!

			// TO DO
			this.filesAndMD5clientHashMap.put(MD5Checksum.createChecksum(listOfFiles[i].getAbsolutePath()),
					listOfFiles[i].getName()); // TO DO
			this.listModelClient.add(i, listOfFiles[i].getName());
		}

	}

	public Map<Byte[], String> getFilesAndMD5clientHashMap() {
		return filesAndMD5clientHashMap;
	}

	public void setSocket(Socket servSocket) {
		myClient = servSocket;
	}

	public void closeSocket() throws IOException {
		myClient.close();
	}

	public Socket getSocket() {
		return myClient;
	}

	public OutputStream getOutputStream() throws IOException {

		return myClient.getOutputStream();

	}

	public InputStream getInputStream() throws IOException {

		return myClient.getInputStream();

	}

	public void setClientNumber(int client) {
		this.clientNumber = client;

	}
	public File getFile ( String fileName){
		File sendFile = null;
		for ( File file : listOfFiles){
			if(file.getName().equals(fileName)){
				sendFile = file;
			}
		}
		return sendFile;
	}
	public void clearModel(){
		for (DefaultListModel model : this.remoteModelLists){
			model.clear();
		}
		this.remoteModelLists.clear();
		for (int x = 0; x < 3; x++) {
			this.remoteModelLists.add(new DefaultListModel<String>());
		}
		this.remoteModelMaps.clear();
		this.filesAndMD5clientHashMap.clear();
		this.listModelClient.clear();

		
	}



}