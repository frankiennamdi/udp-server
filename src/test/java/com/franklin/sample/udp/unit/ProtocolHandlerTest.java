package com.franklin.sample.udp.unit;

import com.franklin.sample.udp.TestSupport;
import com.franklin.sample.udp.message.MessageFragment;
import com.franklin.sample.udp.server.ProtocolHandler;
import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProtocolHandlerTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testProcessMessageFragment() throws IOException, URISyntaxException {
    String dataZip = Paths.get(Resources.getResource("message/data.zip").toURI()).toAbsolutePath().toString();
    String unzipFolder = temporaryFolder.newFolder().getAbsolutePath();
    TestSupport.unzip(dataZip, unzipFolder);
    String testMessageFragment = unzipFolder + "/data/0.txt";
    String data = new String(Files.readAllBytes(Paths.get(testMessageFragment)));
    ProtocolHandler protocolHandler = new ProtocolHandler();
    MessageFragment messageFragment = protocolHandler.processMessageFragment(DatatypeConverter.parseHexBinary(data));
    assertThat(messageFragment.getFlag(), is(0));
    assertThat(messageFragment.getOffset(), is(0L));
    assertThat(messageFragment.getTransactionId(), is(1L));
  }


}
