package edu.usfca.dataflow;

import org.junit.Test;

import com.google.protobuf.Message;

import edu.usfca.protobuf.Profile.DummyKv;
import edu.usfca.protobuf.Profile.KvInt32;

/**
 * .
 */
public class L27Protos {

  static void helperPrint(String prefix, Message msg) {
    byte[] arr = msg.toByteArray();
    System.out.format("%s Size: %d bytes\n", prefix, arr.length);
    for (int i = 0; i < arr.length; i++) {
      System.out.format("%02X ", arr[i]);
      if ((i + 1) % 25 == 0 || i + 1 == arr.length)
        System.out.println();
    }
    System.out.println("---------------------------------------------------------> ");
  }

  @Test
  public void testFieldNumber() {
    DummyKv kv1 = DummyKv.newBuilder().setKey("usf").setVal(123).build();
    KvInt32 kv2 = KvInt32.newBuilder().setKey("usf").setVal(123).build();

    helperPrint("kv1: ", kv1);
    helperPrint("kv2: ", kv2);
  }
}
