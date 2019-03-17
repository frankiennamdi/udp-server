package com.franklin.sample.udp.integration;

import com.franklin.sample.udp.TestSupport;
import com.franklin.sample.udp.app.Launcher;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
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

public class LauncherITest {

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private List<String> packets;

  @Before
  public void setup() throws IOException, URISyntaxException {
    String dataZip = Paths.get(Resources.getResource("message/data.zip").toURI()).toAbsolutePath().toString();
    String unzipFolder = temporaryFolder.newFolder().getAbsolutePath();
    packets = TestSupport.unzip(dataZip, unzipFolder);
  }

  @Test
  public void testUdpServer_withCompletePackets() throws Exception {
    Launcher launcher = new Launcher();
    launcher.start(new String[]{"server", "-p=9999"});
    try (UdpTestClient udpTestClient = new UdpTestClient(9999)) {
      for (String dataFile : packets) {
        String data = new String(Files.readAllBytes(Paths.get(dataFile)));
        udpTestClient.send(DatatypeConverter.parseHexBinary(data));
      }
    }
    TimeUnit.SECONDS.sleep(30);
    launcher.stop();
    String expectedOutput = "Message #1 length: 817923 sha256: d3b20f7cafb7bc626ef5f023975da55779954d2aa3bb3cb4e69ea72ced3504d6\n";
    assertThat(outputCapture.toString(), containsString(expectedOutput));
  }

  @Test
  public void testUdpServer_withIncompletePackets() throws Exception {
    Launcher launcher = new Launcher();
    launcher.start(new String[]{"server", "-p=9999"});
    List<String> packetsToSkip = Lists.newArrayList("1127.txt", "2550.txt");
    try (UdpTestClient udpTestClient = new UdpTestClient(9999)) {
      for (String dataFile : packets) {
        if (packetsToSkip.contains(StringUtils.substringAfterLast(dataFile, "/"))) {
          continue;
        }
        String data = new String(Files.readAllBytes(Paths.get(dataFile)));
        udpTestClient.send(DatatypeConverter.parseHexBinary(data));
      }
    }
    TimeUnit.SECONDS.sleep(30);
    launcher.stop();
    String expectedOutput = "Message #1 Hole at: 2550\n" +
            "Message #1 Hole at: 1127\n";
    assertThat(outputCapture.toString(), containsString(expectedOutput));
  }
}
