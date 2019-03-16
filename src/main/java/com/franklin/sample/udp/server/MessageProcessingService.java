package com.franklin.sample.udp.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
class MessageProcessingService {

  private final ProtocolHandler protocolHandler;

  private final MessageTransactionManager messageTransactionManager;

  private final TaskExecutor executorService;

  @Autowired
  public MessageProcessingService(ProtocolHandler protocolHandler, TaskExecutor taskExecutor) {
    this.protocolHandler = protocolHandler;
    this.messageTransactionManager = new MessageTransactionManager(protocolHandler);
    this.executorService = taskExecutor;
  }

  void processMessage(byte[] bytes) {
    executorService.execute(() -> messageTransactionManager.registerMessageFragmentForTransaction(protocolHandler.processMessage(bytes)));
  }
}
