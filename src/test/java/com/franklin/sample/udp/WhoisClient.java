package com.franklin.sample.udp;

import java.net.*;
import java.io.*;
 
/**
 * This program demonstrates a client socket application that connects
 * to a Whois server to get information about a domain name.
 * @author www.codejava.net
 */
public class WhoisClient {
 
    public static void main(String[] args) {

        String hostname = "localhost";
        int port = 6790;
 
        try (Socket socket = new Socket(hostname, port)) {
 
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("hello");
 
            InputStream input = socket.getInputStream();
 
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
            int line;
            System.out.println("reading");
            char[] buff = new char[1];
            while ((line = reader.read(buff)) != -1) {
                System.out.println(new String(buff));
            }
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}