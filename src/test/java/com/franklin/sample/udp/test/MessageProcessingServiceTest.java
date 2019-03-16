package com.franklin.sample.udp.test;

import com.franklin.sample.udp.app.Launcher;
import com.franklin.sample.udp.message.MessageProcessingService;
import com.franklin.sample.udp.message.MessageTransactionManager;
import com.franklin.sample.udp.server.ProtocolHandler;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.boot.test.rule.OutputCapture;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MessageProcessingServiceTest {

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testProcessMessage() throws URISyntaxException, IOException, InterruptedException {
    Launcher launcher = new Launcher();
    ProtocolHandler protocolHandler = new ProtocolHandler();
    MessageTransactionManager messageTransactionManager = new MessageTransactionManager(protocolHandler);
    MessageProcessingService messageProcessingService = new MessageProcessingService(protocolHandler, launcher.taskExecutor(), messageTransactionManager);

    String dataZip = Paths.get(Resources.getResource("message/data.zip").toURI()).toAbsolutePath().toString();
    String unzipFolder = temporaryFolder.newFolder().getAbsolutePath();
    List<String> dataFiles = TestSupport.unZipIt(dataZip, unzipFolder);
    for (String dataFile : dataFiles) {
      String data = new String(Files.readAllBytes(Paths.get(dataFile)));
      messageProcessingService.processMessage(DatatypeConverter.parseHexBinary(data));
    }
    TimeUnit.SECONDS.sleep(30);
    String expectedOutput = "Message #1 length: 817923 sha256: d3b20f7cafb7bc626ef5f023975da55779954d2aa3bb3cb4e69ea72ced3504d6\n";

    System.out.println(outputCapture.toString());
    assertThat(outputCapture.toString(), containsString(expectedOutput));
  }

  @Test
  public void testProcessMessage_withMissingFragments() throws URISyntaxException, IOException, InterruptedException {
    List<String> packetsToSkip = Lists.newArrayList("1127.txt", "2550.txt");
    Launcher launcher = new Launcher();
    ProtocolHandler protocolHandler = new ProtocolHandler();
    MessageTransactionManager messageTransactionManager = new MessageTransactionManager(protocolHandler);
    MessageProcessingService messageProcessingService = new MessageProcessingService(protocolHandler, launcher.taskExecutor(), messageTransactionManager);

    String dataZip = Paths.get(Resources.getResource("message/data.zip").toURI()).toAbsolutePath().toString();
    String unzipFolder = temporaryFolder.newFolder().getAbsolutePath();
    List<String> dataFiles = TestSupport.unZipIt(dataZip, unzipFolder);
    for (String dataFile : dataFiles) {
      if (packetsToSkip.contains(StringUtils.substringAfterLast(dataFile, "/"))) {
        continue;
      }
      String data = new String(Files.readAllBytes(Paths.get(dataFile)));
      messageProcessingService.processMessage(DatatypeConverter.parseHexBinary(data));
    }
    TimeUnit.SECONDS.sleep(30);
    String expectedOutput = "Message #1 Hole at: 2550\n" +
            "Message #1 Hole at: 1127\n";
    assertThat(outputCapture.toString(), containsString(expectedOutput));
  }
}
