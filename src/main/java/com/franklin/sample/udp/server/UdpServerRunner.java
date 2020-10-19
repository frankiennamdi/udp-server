package com.franklin.sample.udp.server;

import com.franklin.sample.udp.message.MessageProcessingService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

@Component
public class UdpServerRunner implements CommandLineRunner, DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServerRunner.class);

  private static final String PORT_OPTION = "p";

  private final Options options = new Options();

  private final CommandLineParser parser = new DefaultParser();

  private final HelpFormatter formatter = new HelpFormatter();

  private static final String COMMAND = "server";

  private static final String TCP_COMMAND = "tcp-server";

  private final MessageProcessingService messageProcessingService;
  private final TaskExecutor taskExecutor;

  private UdpServer udpServer;

  private EchoTcpServer echoTcpServer;

  @Autowired
  public UdpServerRunner(MessageProcessingService messageProcessingService, TaskExecutor taskExecutor) {
    this.messageProcessingService = messageProcessingService;
    this.taskExecutor = taskExecutor;
    options.addOption(Option.builder(PORT_OPTION)
            .hasArg()
            .longOpt("port")
            .desc("server port")
            .valueSeparator()
            .type(Long.class)
            .optionalArg(true)
            .build());
  }

  @Override
  public void run(String... args) throws Exception {

    if (args.length > 0) {

      if (COMMAND.equals(args[0])) {
        try {
          CommandLine cmd = parser.parse(options, args);
          int port = Integer.parseInt(Optional.ofNullable(cmd.getOptionValue(PORT_OPTION))
                  .orElse(String.valueOf(UdpServer.DEFAULT_PORT)));
          udpServer = new UdpServer(messageProcessingService, port);
          udpServer.start();
          LOGGER.info("running server on port {}", port);

        } catch (ParseException e) {
          LOGGER.error(e.getMessage());
          showUsage();
          System.exit(1);
        }
      } else if (TCP_COMMAND.equals(args[0])) {
        int port = EchoTcpServer.DEFAULT_PORT;
        echoTcpServer = new EchoTcpServer(taskExecutor, port);
        echoTcpServer.start();
        LOGGER.info("running server on port {}", port);
      }
      else {
        showUsage();
        System.exit(1);
      }
    }
  }

  private void showUsage() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    formatter.printUsage(pw, formatter.getWidth(), COMMAND, options);
    formatter.printOptions(pw, formatter.getWidth(), options, formatter.getLeftPadding(),
            formatter.getDescPadding());
    LOGGER.info("\n{}", sw);
  }

  @Override
  public void destroy() throws Exception {
    if (udpServer != null && udpServer.isRunning()) {
      LOGGER.info("Shutdown starting");
      udpServer.shutdown();
      udpServer.join();
      LOGGER.info("Shutdown completed");
    }

    if (echoTcpServer != null && echoTcpServer.isRunning()) {
      LOGGER.info("Shutdown Tcp starting");
      echoTcpServer.shutdown();
      echoTcpServer.join();
      LOGGER.info("Shutdown Tcp completed");
    }
  }
}
