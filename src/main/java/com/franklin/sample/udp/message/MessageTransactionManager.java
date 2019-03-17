package com.franklin.sample.udp.message;

import com.franklin.sample.udp.server.ProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MessageTransactionManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageTransactionManager.class);

  private final Map<Long, Set<MessageFragment>> transactionRegister = new HashMap<>();

  private final ProtocolHandler protocolHandler;

  private final int maxTransactionWaitTimeMilli = 30000;

  @Autowired
  public MessageTransactionManager(ProtocolHandler protocolHandler) {
    this.protocolHandler = protocolHandler;
  }

  public void registerMessageFragmentForTransaction(MessageFragment messageFragment) {
    synchronized (transactionRegister) {
      transactionRegister.computeIfAbsent(messageFragment.getTransactionId(), ign -> {
        Timer timer = new Timer();
        timer.schedule(new MessageTransactionCheckerTask(timer, this, messageFragment.getTransactionId()),
                maxTransactionWaitTimeMilli);
        return new TreeSet<>(Comparator.comparing(MessageFragment::getOffset));
      }).add(messageFragment);
    }
  }

  private Set<MessageFragment> getMessageFragments(long transactionId) {
    synchronized (transactionRegister) {
      return transactionRegister.getOrDefault(transactionId, new TreeSet<>(Comparator.comparing(MessageFragment::getOffset)));
    }
  }

  public MessageStatus getMessageStatus(long transactionId) {
    synchronized (transactionRegister) {
      Set<MessageFragment> messageFragments = getMessageFragments(transactionId);
      if (messageFragments.isEmpty()) {
        return new MessageStatus(transactionId);
      } else {
        Iterator<MessageFragment> iterator = messageFragments.iterator();
        MessageFragment current = null;
        MessageFragment next = null;

        if (iterator.hasNext()) {
          current = iterator.next();
        }

        if (current == null || current.getOffset() != 0) {
          return new MessageStatus(transactionId);
        }

        MessageStatus messageStatus = new MessageStatus(transactionId);
        messageStatus.updateFlag(current.getFlag());

        if (iterator.hasNext()) {
          next = iterator.next();
          messageStatus.updateFlag(next.getFlag());
          long expectedNextOffset = current.getOffset() + current.getData().length;
          if ((next.getOffset() != expectedNextOffset)) {
            messageStatus.updateMissingFragments(expectedNextOffset);
          }
        }

        while (iterator.hasNext()) {
          current = next;
          next = iterator.next();
          messageStatus.updateFlag(next.getFlag());
          long expectedNextOffset = current.getOffset() + current.getData().length;
          if ((next.getOffset() != expectedNextOffset)) {
            messageStatus.updateMissingFragments(expectedNextOffset);
          }
        }
        return messageStatus;
      }
    }
  }

  public void closeTransaction(long transactionId) {
    synchronized (transactionRegister) {
      MessageStatus messageStatus = getMessageStatus(transactionId);
      if (messageStatus.isCompleted()) {

        Set<MessageFragment> messageFragments = getMessageFragments(transactionId);
        byte[] accumulatedBytes = protocolHandler.concatenateByteArrays(
                messageFragments.stream().map(MessageFragment::getData).collect(Collectors.toList()));
        String sha1 = protocolHandler.bytesToSha256(accumulatedBytes);
        LOGGER.info("Message #{} length: {} sha256: {}", transactionId, accumulatedBytes.length, sha1);
      } else {

        messageStatus.getMissingFragments().forEach(e -> LOGGER.info("Message #{} Hole at: {}",
                messageStatus.getTransactionId(), e));
      }
      transactionRegister.remove(transactionId);
    }
  }

  public static class MessageTransactionCheckerTask extends TimerTask {

    private final Timer timer;

    private final MessageTransactionManager transactionManager;

    private final long transactionId;

    public MessageTransactionCheckerTask(Timer timer, MessageTransactionManager transactionManager, long transactionId) {
      this.timer = timer;
      this.transactionManager = transactionManager;
      this.transactionId = transactionId;
    }

    @Override
    public void run() {
      transactionManager.closeTransaction(transactionId);
      timer.cancel();
    }
  }
}
