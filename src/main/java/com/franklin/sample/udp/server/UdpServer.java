package com.franklin.sample.udp.server;

import com.franklin.sample.udp.message.MessageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

class UdpServer extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

  static final int DEFAULT_PORT = 6789;

  private static final int WAIT_TIMEOUT = 3000;

  private static final int BUF_SIZE = 1024;

  private final MessageProcessingService messageProcessingService;

  private final int serverPort;

  private volatile boolean run = false;

  UdpServer(MessageProcessingService messageProcessingService, int severPort) {
    super("udp-server-main");
    this.messageProcessingService = messageProcessingService;
    this.serverPort = severPort;
  }

  @Override
  public void run() {
    try {
      run = true;
      listen(serverPort);
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }

  void shutdown() {
    run = false;
  }

  private void listen(int serverPort) throws IOException {
    try (DatagramChannel channel = DatagramChannel.open(); DatagramSocket socket = channel.socket();
         Selector selector = Selector.open()) {
      channel.configureBlocking(false);
      socket.bind(new InetSocketAddress(serverPort));
      channel.register(selector, SelectionKey.OP_READ);
      LOGGER.info("Server Started");
      while (run) {
        if (selector.select(WAIT_TIMEOUT) == 0) {
          continue;
        }
        final Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
          final SelectionKey key = keyIterator.next();
          if (key.isReadable()) {
            messageProcessingService.processMessage(read(key));
          }
          keyIterator.remove();
        }
      }
    }
    LOGGER.info("Server Shutdown");
  }

  private byte[] read(SelectionKey key) throws IOException {
    DatagramChannel channel = (DatagramChannel) key.channel();
    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
    buf.clear();
    channel.receive(buf);
    return buf.array();
  }
}
