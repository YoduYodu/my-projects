package edu.usfca.dataflow.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

/**
 * This class contains various utility methods that deal with Proto messages.
 */
public class ProtoUtils {
  /**
   * Returns the number of bytes required to encode a given proto message {@code msg}.
   *
   * @throws IllegalArgumentException if msg is null.
   */
  static int getSerializedSize(Message msg) {
    if (msg == null) {
      throw new IllegalArgumentException();
    }
    return msg.getSerializedSize();
  }

  /**
   * Returns Json representation (in String with no insignificant whitespace(s)) of a given proto message {@code msg}.
   *
   * @throws IllegalArgumentException if msg is null.
   * @throws InvalidProtocolBufferException if msg contains types that cannot be processed/printed.
   */
  public static String getJsonFromMessage(Message msg) throws InvalidProtocolBufferException {
    if (msg == null) {
      throw new IllegalArgumentException();
    }
    return JsonFormat.printer().preservingProtoFieldNames().omittingInsignificantWhitespace().print(msg);
  }
}
