package com.netServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ResolveThread extends Thread {
	ServiceThread wantsSomeService;
	ServiceThread haveSomethingService;
	String fileName;
	private ArrayList<ServiceThread> serviceThreadList;
	// private static ArrayList<ResolveThread> resolveThreadList; NOT NEEDED NOW
	// BUT WHEN MORE CLIENTS
	public boolean resolveNeeded;
	public boolean serverAlive;
	public int resolveNumber;
	private int clientServSocket;

	public ResolveThread() {
		this.resolveNeeded = false;
		this.serverAlive = true;
		this.serviceThreadList = new ArrayList<ServiceThread>(3);
		for (int i = 0; i < 4; i++) {
			serviceThreadList.add(null);
		}

	}

	public void run(){
		//COMUNICATE EACH OTHER WITH THHEIR SOCKETS AND STREAMS
		while (serverAlive){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
					if(resolveNeeded){
						
						switch(resolveNumber){
										//wantSomeService want to GET file from haveSomeService
										case 331:	
											log("SERVER RESOLVER: FOR HOST " +wantsSomeService.getClientNumber()+
													" PULL FILE  "+ fileName + " FROM HOST "+ haveSomethingService.getClientNumber());
											try{	
												haveSomethingService.dOut.writeInt(331);
												haveSomethingService.dOut.writeUTF(fileName);
												
												haveSomethingService.dOut.writeUTF(wantsSomeService.getSocket().getInetAddress().getHostAddress());
												haveSomethingService.dOut.writeInt(clientServSocket);
											} catch (IOException e) {
												
												e.printStackTrace();
											}
												resolveNeeded = false;
												break;
											
										case 441:
											log("SERVER RESOLVER: FOR HOST " +wantsSomeService.getClientNumber()+
													" PUSH FILE  "+ fileName + " TO HOST "+haveSomethingService.getClientNumber());
											try{	
												haveSomethingService.dOut.writeInt(441);
												haveSomethingService.dOut.writeUTF(fileName);
												haveSomethingService.dOut.writeUTF(wantsSomeService.getSocket().getInetAddress().getHostAddress());
												haveSomethingService.dOut.writeInt(clientServSocket);
											} catch (IOException e) {
												
												e.printStackTrace();
											}
												resolveNeeded = false;
												break;
						}
					
						
						
					}
					
					
		}
		
	
	}

	public void getFile(int fileSize) throws IOException {

	}

	public void addServiceToList(ServiceThread temporaryService,
			Integer servNumb) {
		this.serviceThreadList.set(servNumb, temporaryService);

	}

	public void rmServiceFromList(ServiceThread temporaryService) {
		this.serviceThreadList.set(temporaryService.getClientNumber(), null);

	}

	public void setNeedResolve(ServiceThread serv, int wantsFromServiceNumb,
			int problemNumb, String name) {

		this.wantsSomeService = serv;
		this.haveSomethingService = serviceThreadList.get(wantsFromServiceNumb);
		this.resolveNeeded = true;
		this.resolveNumber = problemNumb;
		this.fileName = name;

	}

	public void setNeedResolve(ServiceThread serv, int wantsFromServiceNumb,
			int portNumb, int problemNumb, String name) {

		this.wantsSomeService = serv;
		this.haveSomethingService = serviceThreadList.get(wantsFromServiceNumb);
		this.resolveNeeded = true;
		this.resolveNumber = problemNumb;
		this.fileName = name;
		this.clientServSocket = portNumb;
		System.out.println("INFO: serv = "+ serv.getClientNumber() +" wantsFromService = "+wantsFromServiceNumb +" port = "+portNumb+
				"problemNumb = "+problemNumb +" name ="+name);
	}

	private void log(String message) {
		System.out.println(message);
	}

	public void clientDead(ServiceThread serv) {
		serviceThreadList.set(serv.getClientNumber(), null);

	}
}