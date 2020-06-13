package edu.usfca.dataflow.utils;

import java.util.Base64;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

/**
 * This class contains various utility methods that deal with Proto messages.
 */
public class ProtoUtils {
  /**
   * Returns a String that encodes a given proto message's byte array in Base64, according to RFC4648.
   *
   * See Javadoc of java.util.Base64 for more details.
   *
   * @throws IllegalArgumentException if {@code msg} is null.
   */
  public static String encodeMessageBase64(Message msg) {
    if (msg == null) {
      throw new IllegalArgumentException("msg is null");
    }
    return Base64.getEncoder().encodeToString(msg.toByteArray());
  }

  /**
   * Returns a proto message by decoding a given encoded message in Base64 (RFC4648).
   *
   * This method should never return null (but it may return a default instance of a proto message).
   *
   * @throws IllegalArgumentException if {@code encodedMsg} is null.
   *
   * @throws InvalidProtocolBufferException if {@code encodedMsg} cannot be parsed using the provided parser.
   */
  public static <T> T decodeMessageBase64(Parser<T> parser, String encodedMsg) throws InvalidProtocolBufferException {
    if (encodedMsg == null) {
      throw new IllegalArgumentException("msg is null");
    }
    return parser.parseFrom(Base64.getDecoder().decode(encodedMsg));
  }

  /**
   * Returns Json representation (in String with no insignificant whitespace(s)) of a given proto message {@code msg}.
   *
   * @throws IllegalArgumentException if msg is null.
   * @throws InvalidProtocolBufferException if msg contains types that cannot be processed/printed.
   */
  public static String getJsonFromMessage(Message msg, boolean omitSpace) throws InvalidProtocolBufferException {
    if (msg == null) {
      throw new IllegalArgumentException();
    }
    Printer printer = JsonFormat.printer().preservingProtoFieldNames();
    if (omitSpace) {
      printer = printer.omittingInsignificantWhitespace();
    }
    return printer.print(msg);
  }

  public static String getJsonFromMessage(Message msg) throws InvalidProtocolBufferException {
    return getJsonFromMessage(msg, false);
  }
}
