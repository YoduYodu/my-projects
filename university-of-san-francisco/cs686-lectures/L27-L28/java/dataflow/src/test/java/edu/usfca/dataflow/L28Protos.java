package edu.usfca.dataflow;

import org.junit.Test;

import com.google.protobuf.Message;

import edu.usfca.protobuf.Profile.Data;
import edu.usfca.protobuf.Profile.Data.MapFieldEntry;
import edu.usfca.protobuf.Profile.Data1;
import edu.usfca.protobuf.Profile.Data2;
import edu.usfca.protobuf.Profile.KvFixed32;
import edu.usfca.protobuf.Profile.KvInt32;
import edu.usfca.protobuf.Profile.KvSint32;

/**
 * .
 */
public class L28Protos {

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

  public void testHelper(int x) {
    KvInt32 kv1 = KvInt32.newBuilder().setVal(x).build();
    KvSint32 kv2 = KvSint32.newBuilder().setVal(x).build();
    KvFixed32 kv3 = KvFixed32.newBuilder().setVal(x).build();

    helperPrint(String.format("  int32: (for number %d)", x), kv1);
    helperPrint(String.format(" sint32: (for number %d)", x), kv2);
    helperPrint(String.format("fixed32: (for number %d)", x), kv3);
  }

  @Test
  public void testSmallNumber() {
    testHelper(70);
  }

  @Test
  public void testMediumNumber() {
    testHelper(10123);
  }

  @Test
  public void testLargeNumber() {
    testHelper(2147483647);
  }

  @Test
  public void testNegativeNumber() {
    testHelper(-1);
  }

  @Test
  public void testNegativeNumber2() {
    testHelper(-2147383648);
  }

  // ------------------------------------------

  @Test
  public void testMaps1() {
    Data data1 = Data.newBuilder()//
        .addMapField(MapFieldEntry.newBuilder().setKey("C").setValue(70).build())//
        .addMapField(MapFieldEntry.newBuilder().setKey("S").setValue(80).build())//
        .build();

    Data data2 = Data.newBuilder().putMyMap("C", 70).putMyMap("S", 80).build();

    helperPrint("data1 ", data1);
    helperPrint("data2 ", data2);
  }

  @Test
  public void testMaps2() {
    Data1 data1 = Data1.newBuilder().putMyMap("C", 70).putMyMap("S", 80).build();

    Data2 data2 = Data2.newBuilder().addMyKey("C").addMyVal(70) //
        .addMyKey("S").addMyVal(80).build();

    helperPrint("data1 ", data1);
    helperPrint("data2 ", data2);
  }
}
