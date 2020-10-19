package com.franklin.sample.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class CrunchifyNIOClient {
 
	public static void main(String[] args) throws IOException, InterruptedException {
 
		InetSocketAddress crunchifyAddr = new InetSocketAddress("localhost", 6790);
		SocketChannel crunchifyClient = SocketChannel.open(crunchifyAddr);
		//crunchifyClient.configureBlocking(false);
 
		log("Connecting to Server on port 6790...");
 
		ArrayList<String> companyDetails = new ArrayList<String>();
 
		// create a ArrayList with companyName list
		companyDetails.add("Facebook");
    companyDetails.add("Johnny");
    for (String companyName : companyDetails) {
 
			byte[] message = companyName.getBytes();
      ByteBuffer buffer = ByteBuffer.wrap(message);
      String response;
      try {
        crunchifyClient.write(buffer);
        buffer.clear();
        System.out.println("receiving");
        buffer = ByteBuffer.allocate(companyName.getBytes().length);
        crunchifyClient.read(buffer);
        System.out.println("received");
        response = new String(buffer.array()).trim();
        System.out.println("response=" + response);
        buffer.clear();
      } catch (IOException e) {
        e.printStackTrace();
      }

			// wait for 2 seconds before sending next message
			Thread.sleep(1000);
		}
		crunchifyClient.close();
	}

 
	private static void log(String str) {
		System.out.println(str);
	}
}