package com.franklin.sample.udp.server;

import java.util.Objects;

/**
 * Threadsafe class that holds a fragment of a message
 */
public class MessageFragment {

  private int flag;

  private long offset;

  private long transactionId;

  private byte[] data;

  public MessageFragment(int flag, long offset, long transactionId, byte[] data) {
    this.flag = flag;
    this.offset = offset;
    this.transactionId = transactionId;
    this.data = data;
  }

  public int getFlag() {
    return flag;
  }

  public long getOffset() {
    return offset;
  }

  public long getTransactionId() {
    return transactionId;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageFragment that = (MessageFragment) o;
    return transactionId == that.transactionId && offset == that.offset &&
            data.length == that.data.length;
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId, offset, data.length);
  }
}
