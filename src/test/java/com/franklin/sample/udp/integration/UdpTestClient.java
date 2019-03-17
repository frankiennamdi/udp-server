package com.franklin.sample.udp.integration;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;

public class UdpTestClient implements Closeable {

  private final DatagramSocket socket;

  private final InetAddress address;

  private final int port;

  public UdpTestClient(int port) throws SocketException, UnknownHostException {
    socket = new DatagramSocket();
    address = InetAddress.getByName("localhost");
    this.port = port;
  }

  public void send(byte[] bytes) throws IOException {
    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
    socket.send(packet);
  }

  public void close() {
    socket.close();
  }
}