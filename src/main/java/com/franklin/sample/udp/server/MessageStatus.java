package com.franklin.sample.udp.server;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Not Threadsafe
 */
public class MessageStatus {

  private static final int EOF_FLAG = 32768;

  private int maxFlag = 0;

  private Set<Long> missingFragments = Sets.newHashSet();

  private final long transactionId;

  public MessageStatus(long transactionId) {
    this.transactionId = transactionId;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public Set<Long> getMissingFragments() {
    return ImmutableSet.copyOf(missingFragments);
  }

  public void updateFlag(int flag) {
    if (flag > maxFlag) {
      maxFlag = flag;
    }
  }

  public void updateMissingFragments(long nextOffset) {
    missingFragments.add(nextOffset);
  }

  public boolean isCompleted() {
    return maxFlag == EOF_FLAG && missingFragments.isEmpty();
  }
}