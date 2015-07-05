package com.netDownloader.Helpers;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Checksum {

	   public static Byte[] createChecksum(String filename) throws Exception {
	       InputStream fis =  new FileInputStream(filename);

	       byte[] buffer = new byte[1024];
	       MessageDigest complete = MessageDigest.getInstance("MD5");
	       int numRead;

	       do {
	           numRead = fis.read(buffer);
	           if (numRead > 0) {
	               complete.update(buffer, 0, numRead);
	           }
	       } while (numRead != -1);

	       fis.close();
	       
	       byte[] md5Array = complete.digest();
			Byte[] byteMd5Boxed = new Byte[md5Array.length];
			int y = 0;
			// Change byte[] to Byte[]
			for (byte b : md5Array)
				byteMd5Boxed[y++] = b; // Autoboxing.
	       return byteMd5Boxed;
	   }

	   
	   public static String getMD5Checksum(String filename) throws Exception {
	       Byte[] b = createChecksum(filename);
	       String result = "";

	       for (int i=0; i < b.length; i++) {
	           result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	       }
	       return result;
	   }
}