package com.franklin.sample.udp.test.server;

import com.franklin.sample.udp.server.MessageFragment;
import com.franklin.sample.udp.server.MessageStatus;
import com.franklin.sample.udp.server.MessageTransactionManager;
import com.franklin.sample.udp.server.ProtocolHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MessageTransactionManagerTest {

  private MessageTransactionManager messageTransactionManager = new MessageTransactionManager(new ProtocolHandler());

  @Test
  public void testHandlingMessageTransaction_withCompleteMessage() throws IOException, URISyntaxException {
    registerMessageFragment("message/sample_message_with_data.csv", ImmutableList.of());
    MessageStatus messageStatus = messageTransactionManager.getMessageStatus(1);
    assertThat(messageStatus.isCompleted(), is(true));
  }

  @Test
  public void testHandlingMessageTransaction_withMissingMessage() throws IOException, URISyntaxException {
    List<Long> offSetToSkip = ImmutableList.of(21004L, 69952L, 54796L, 49330L);
    registerMessageFragment("message/sample_message_with_data.csv", offSetToSkip);
    MessageStatus messageStatus = messageTransactionManager.getMessageStatus(1);
    assertThat(messageStatus.isCompleted(), is(false));
    assertThat(messageStatus.getMissingFragments().size(), is(offSetToSkip.size()));
    assertThat(messageStatus.getMissingFragments(), containsInAnyOrder(offSetToSkip.toArray()));
  }

  @Test
  public void testHandlingMessageTransaction_withMissingLastMessage() throws IOException, URISyntaxException {
    List<Long> offSetToSkip = ImmutableList.of(71619L);
    registerMessageFragment("message/sample_message_with_data.csv", offSetToSkip);
    MessageStatus messageStatus = messageTransactionManager.getMessageStatus(1);
    assertThat(messageStatus.isCompleted(), is(false));
  }


  @Test
  public void testHandlingMessageTransaction_withMissingFirstMessage() throws IOException, URISyntaxException {
    List<Long> offSetToSkip = ImmutableList.of(0L);
    registerMessageFragment("message/sample_message_with_data.csv", offSetToSkip);
    MessageStatus messageStatus = messageTransactionManager.getMessageStatus(1);
    assertThat(messageStatus.isCompleted(), is(false));
  }

  private void registerMessageFragment(String resource, List<Long> offSetToSkip) throws IOException, URISyntaxException {

    try (Reader reader = Files.newBufferedReader(
            Paths.get(Resources.getResource(resource).toURI()));
         CSVReader csvReader = new CSVReaderBuilder(reader)
                 .withCSVParser(new CSVParserBuilder()
                         .withSeparator('\t')
                         .build()).build()
    ) {
      String[] nextRecord;

      while ((nextRecord = csvReader.readNext()) != null) {
        long offSet = Long.valueOf(nextRecord[1]);
        if (offSetToSkip.contains(offSet)) {
          continue;
        }
        MessageFragment messageFragment = new MessageFragment(Integer.parseInt(nextRecord[2]), offSet, Long.valueOf(nextRecord[0]),
                DatatypeConverter.parseHexBinary(nextRecord[4]));
        messageTransactionManager.registerMessageFragmentForTransaction(messageFragment);
      }
    }
  }
}