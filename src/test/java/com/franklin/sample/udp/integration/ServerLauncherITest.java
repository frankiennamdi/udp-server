package com.franklin.sample.udp.integration;

import com.franklin.sample.udp.ServerLauncher;
import com.franklin.sample.udp.TestSupport;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
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
import static org.junit.Assert.assertThat;

public class ServerLauncherITest {

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private List<String> packets;

  private ServerLauncher serverLauncher;

  private int port = 6789;

  @Before
  public void setup() throws IOException, URISyntaxException {
    String dataZip = Paths.get(Resources.getResource("message/data.zip").toURI()).toAbsolutePath().toString();
    String unzipFolder = temporaryFolder.newFolder().getAbsolutePath();
    packets = TestSupport.unzip(dataZip, unzipFolder);
    serverLauncher = new ServerLauncher();
    serverLauncher.start(new String[]{"server", "-p=" + port});
  }

  @After
  public void shutdown() {
    serverLauncher.stop();
  }

  @Test
  public void testUdpServer_withCompletePackets() throws Exception {
    try (UdpTestClient udpTestClient = new UdpTestClient(port)) {
      for (String dataFile : packets) {
        String data = new String(Files.readAllBytes(Paths.get(dataFile)));
        udpTestClient.send(DatatypeConverter.parseHexBinary(data));
      }
    }
    TimeUnit.SECONDS.sleep(30);
    String expectedOutput = "Message #1 length: 817923 sha256: d3b20f7cafb7bc626ef5f023975da55779954d2aa3bb3cb4e69ea72ced3504d6\n";
    assertThat(outputCapture.toString(), containsString(expectedOutput));
  }

  @Test
  public void testUdpServer_withIncompletePackets() throws Exception {
    List<String> packetsToSkip = Lists.newArrayList("1127.txt", "2550.txt");
    try (UdpTestClient udpTestClient = new UdpTestClient(port)) {
      for (String dataFile : packets) {
        final String fileName = StringUtils.substringAfterLast(dataFile, "/");
        if (packetsToSkip.stream().anyMatch(file -> file.equals(fileName))) {
          continue;
        }
        String data = new String(Files.readAllBytes(Paths.get(dataFile)));
        udpTestClient.send(DatatypeConverter.parseHexBinary(data));
      }
    }
    TimeUnit.SECONDS.sleep(30);
    String expectedOutput = "Message #1 Hole at: 2550\nMessage #1 Hole at: 1127\n";
    assertThat(outputCapture.toString(), containsString(expectedOutput));
  }
}
