package edu.usfca.dataflow;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import edu.usfca.protobuf.Common.NotNested;
import edu.usfca.protobuf.Common.SearchRequest.Nested;

public class Main {

  static void helperPrint(Message msg) {
    byte[] arr = msg.toByteArray();
    System.out.format("Size: %d bytes\n", arr.length);
    for (int i = 0; i < arr.length; i++) {
      System.out.format("%03d ", (int) arr[i]);
      if ((i + 1) % 10 == 0 || i + 1 == arr.length)
        System.out.println();
    }
    System.out.println("-----------------------------------");
  }

  // main method is not used for this specific assignment.
  public static void main(String[] args) throws InvalidProtocolBufferException {
    // Code used for lecture 02.

    // Empty messages -> Empty byte arrays.
    helperPrint(Nested.getDefaultInstance());
    helperPrint(Nested.newBuilder().build());

    // Messages (of different types) but with same values.
    // Once these are serialized into byte arrays, they are no longer distinguishable.
    helperPrint(Nested.newBuilder().setSomeId("cs686").setSomeValue(123L).build());
    helperPrint(NotNested.newBuilder().setSomeId("cs686").setSomeValue(123L).build());

    // Messages from byte arrays (deserialization).
    // Assuming that the provided byte arrays represent Nested or NotNested messages,
    // we can deserialize them using parseFrom() method that takes byte[].
    Nested msg1 = Nested.parseFrom(new byte[] {10, 5, 99, 115, 54, 56, 54, 16, 123});
    NotNested msg2 = NotNested.parseFrom(new byte[] {10, 5, 99, 115, 54, 56, 54, 16, 123});

    // Message contents via toString() (useful for debugging)
    System.out.println(msg1.toString());
    System.out.println(msg2.toString());
    System.out.println(msg1.equals(msg2)); // Of course, false.

    // Message contents via JSON() (useful for many things)
    // There are many options for convenience, and you should check out what options are provided.
    // In this course, it's recommended that you always use 'preservingProtoFieldNames()' for consistency.
    System.out.println("--------------------------------");
    System.out.println(JsonFormat.printer().print(msg1));
    System.out.println(JsonFormat.printer().preservingProtoFieldNames().print(msg1));
    System.out.println(JsonFormat.printer().preservingProtoFieldNames().includingDefaultValueFields().print(msg1));

    // Additional Examples:
    // Compare byte arrays of these messages.
    // Try a few examples yourself and share your examples on Piazza.
    // We won't go into the details of how Protocol Buffers are implemented in this course,
    // and we'll merely use it as a black-box tool.
    System.out.println("--------------------------------");
    helperPrint(Nested.newBuilder().setSomeId("cs686").setSomeValue(1L).build());
    helperPrint(Nested.newBuilder().setSomeId("cs686").setSomeValue(12L).build());
    helperPrint(Nested.newBuilder().setSomeId("cs686").setSomeValue(123L).build());
    helperPrint(Nested.newBuilder().setSomeValue(123L).build());
    helperPrint(Nested.newBuilder().setSomeValue(0).build());
  }
}
