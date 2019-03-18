package com.franklin.sample.udp.message;

import com.franklin.sample.udp.server.ProtocolHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessingService {

  private final ProtocolHandler protocolHandler;

  private final MessageTransactionManager messageTransactionManager;

  private final TaskExecutor executorService;

  @Autowired
  public MessageProcessingService(ProtocolHandler protocolHandler, TaskExecutor taskExecutor,
                                  MessageTransactionManager messageTransactionManager) {
    this.protocolHandler = protocolHandler;
    this.messageTransactionManager = messageTransactionManager;
    this.executorService = taskExecutor;
  }

  public void processMessage(byte[] bytes) {
    executorService.execute(() -> messageTransactionManager.registerMessageFragmentForTransaction(
            protocolHandler.processMessageFragment(bytes)));
  }
}
