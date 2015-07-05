package com.netServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultListModel;

public class ServerMain {
	private static int clientAmount = 0;
	public static void main(String[] args) throws Exception{
		
		ServerSocket server_socket = new ServerSocket(10500);
		ResolveThread serverResolver = new ResolveThread();
		//int clientNumber = 0;
		
		try {
			
			while (true) {
				if(clientAmount == 0){
					serverResolver.start();
				}
				

				ServiceThread temporaryService = new ServiceThread(server_socket.accept(), serverResolver);
				temporaryService.start();
				
				clientAmount++;
				
				
				
			}
		} finally {
            server_socket.close();
        }
	}

	

	


}

