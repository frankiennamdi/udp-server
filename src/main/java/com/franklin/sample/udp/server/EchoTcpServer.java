package com.franklin.sample.udp.server;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

class EchoTcpServer extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(EchoTcpServer.class);

  static final int DEFAULT_PORT = 6790;

  private static final int WAIT_TIMEOUT = 5000;

  private static final int BUF_SIZE = 1024;

  private final int serverPort;
  private final TaskExecutor taskExecutor;

  private volatile boolean running;

  EchoTcpServer(TaskExecutor taskExecutor, int severPort) {
    super("tcp-server-main");
    this.serverPort = severPort;
    this.taskExecutor = taskExecutor;
  }

  @Override
  public void run() {
    running = true;
    listenOn(serverPort);
  }

  void shutdown() {
    running = false;
  }

  boolean isRunning() {
    return running;
  }

  private void listenOn(int serverPort) {
    try (ServerSocketChannel channel = ServerSocketChannel.open();
         Selector selector = Selector.open()) {
      LOGGER.info("{}", serverPort);
      channel.bind(new InetSocketAddress(serverPort));
      channel.configureBlocking(false);
      channel.register(selector, SelectionKey.OP_ACCEPT);
      LOGGER.info("Server Started");
      while (running) {
        if (selector.select(WAIT_TIMEOUT) == 0) {
          continue;
        }
        final Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
          final SelectionKey key = keyIterator.next();
          if (key.isAcceptable()) {
            SocketChannel crunchifyClient = channel.accept();

            // Adjusts this channel's blocking mode to false
            crunchifyClient.configureBlocking(false);

            // Operation-set bit for read operations
            crunchifyClient.register(selector, SelectionKey.OP_READ);
            LOGGER.info("Connection Accepted: {}\n" + crunchifyClient.getLocalAddress());
            keyIterator.remove();

            // Tests whether this key's channel is ready for reading
          } else if (key.isReadable()) {
            byte[] received = read(key);
            if (received.length < 1 || Strings.isNullOrEmpty(new String(received).trim())) {
              keyIterator.remove();
              continue;
            }

            taskExecutor.execute(() -> {
              LOGGER.info("Received: {}\n", new String(received));
              try {
                write(key, received);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
            keyIterator.remove();

            /*taskExecutor.execute(() -> {

            });*/
          }

        }
      }
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e);
    } finally {
      LOGGER.info("Server Shutdown");
    }
  }

  private void write(SelectionKey key, byte[] data) throws IOException {
    //LOGGER.info("writing data");
    SocketChannel channel = (SocketChannel) key.channel();
    ByteBuffer buf = ByteBuffer.wrap(data);
    buf.flip();
    channel.write(buf);
    buf.clear();
    LOGGER.info("written");
  }

  private byte[] read(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
    buf.clear();
    channel.read(buf);
    return buf.array();
  }
}
