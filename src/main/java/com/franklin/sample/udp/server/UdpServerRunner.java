package com.franklin.sample.udp.server;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

@Component
public class UdpServerRunner implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServerRunner.class);

  private static final String PORT_OPTION = "p";

  private final Options options = new Options();

  private final CommandLineParser parser = new DefaultParser();

  private final HelpFormatter formatter = new HelpFormatter();

  private static final String COMMAND = "server";

  private final MessageProcessingService messageProcessingService;

  private UdpServer udpServer;

  @Autowired
  public UdpServerRunner(MessageProcessingService messageProcessingService) {
    this.messageProcessingService = messageProcessingService;
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
    int port = UdpServer.DEFAULT_PORT;
    if (args.length > 0) {
      if (COMMAND.equals(args[0])) {
        try {
          CommandLine cmd = parser.parse(options, args);
          port = Integer.parseInt(Optional.ofNullable(cmd.getOptionValue(PORT_OPTION)).orElse(String.valueOf(UdpServer.DEFAULT_PORT)));

        } catch (ParseException e) {
          LOGGER.error(e.getMessage());
          showUsage();
          System.exit(1);
        }
      } else {
        showUsage();
        System.exit(1);
      }
    }
    udpServer = new UdpServer(messageProcessingService, port);
    udpServer.start();
    LOGGER.info("running server on port {}", port);
  }

  private void showUsage() {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    formatter.printUsage(pw, formatter.getWidth(), COMMAND, options);
    formatter.printOptions(pw, formatter.getWidth(), options, formatter.getLeftPadding(),
            formatter.getDescPadding());
    LOGGER.info("\n{}", sw);
  }

  @PreDestroy
  public void onDestroy() throws Exception {
    LOGGER.info("Shutdown starting");
    if (udpServer != null) {
      udpServer.shutdown();
      udpServer.join();
    }
    LOGGER.info("Shutdown completed");
  }
}
