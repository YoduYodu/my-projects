package edu.usfca.dataflow.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.App;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Geo;

import edu.usfca.dataflow.__TestBase;
import edu.usfca.protobuf.Bid.BidLog;
import edu.usfca.protobuf.Bid.BidResult;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;

/**
 * Instructions:
 *
 * These unit tests should pass, after you fix a bug in each method found in BidLogUtils class.
 *
 * If you choose to write your own methods, make sure you test them.
 */
public class __Test01BidLogUtils {
  // Note: This rule is used to stop the grading system from going down when your code gets stuck (safety measure).
  // If you think this prevents your submission from getting graded normally, ask on Piazza.
  @Rule
  public Timeout timeout = Timeout.millis(2000);

  @Test(expected = IllegalArgumentException.class)
  public void testGetDeviceProfileException() {
    BidLog valid1;
    BidLog.Builder bl = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
        .setReceivedAt(12345L).setProcessedAt(123456L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)));
    assertTrue(BidLogUtils.isValid(valid1 = bl.build()));
    bl.setBidResult(BidResult.NO_CANDIDATE).setBidPrice(0);

    BidLogUtils.getDeviceProfile(valid1.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build());
  }

  @Test
  public void testIsValid01() {
    BidLog valid;
    BidLog.Builder bl = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
        .setReceivedAt(12345L).setProcessedAt(123456L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)).build());
    assertTrue(BidLogUtils.isValid(valid = bl.build()));

    // Test cases with invalid samples.
    assertFalse(BidLogUtils.isValid(valid.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build()));
    assertFalse(BidLogUtils.isValid(valid.toBuilder().setBidPrice(-1).build()));
    assertFalse(BidLogUtils.isValid(valid.toBuilder().setReceivedAt(-1).build()));
    assertFalse(BidLogUtils.isValid(valid.toBuilder().setProcessedAt(1234L).build()));

    // NOTE: Pay attention to the requirements for BidLogUtils.isValid() method.
    // If it passes a hidden test in this class, then your method is likely to be correct.
  }
  // ---------

  @Test
  public void testIsValid02() {
    BidLog valid1, valid2;
    BidRequest.Builder br;
    Device.Builder device;
    App.Builder app;
    // Test cases with valid samples.
    {
      BidLog.Builder bl = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
          .setReceivedAt(12345L).setProcessedAt(123456L)
          .setBidRequest(br = BidRequest.newBuilder().setId("bidid")
              .setDevice(device = Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                  .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
              .setApp(app = App.newBuilder().setBundle(__TestBase.Bundle1)));
      assertTrue(BidLogUtils.isValid(valid1 = bl.build()));
      bl.setBidResult(BidResult.NO_CANDIDATE).setBidPrice(0);
      assertTrue(BidLogUtils.isValid(valid2 = bl.build()));
    }

    // Test cases with invalid samples.
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidResult(BidResult.UNKNOWN_RESULT).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setBidResult(BidResult.UNKNOWN_RESULT).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidPrice(0).build()));
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidPrice(-1).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidRequest(br.setDevice(device.setOs("iios"))).build()));
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidRequest(br.setDevice(device.clearOs())).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setBidRequest(br.setDevice(device.setOs("iios"))).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setBidRequest(br.setDevice(device.clearOs())).build()));

    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setReceivedAt(0).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setReceivedAt(-1).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidRequest(br.setApp(app.clearBundle())).build()));
  }

  // ---------

  @Test
  public void testIsValid03() {
    BidLog valid1, valid2;
    BidRequest br;
    Device device;
    App app;
    Geo geo;
    // Test cases with valid samples.
    {
      app = App.newBuilder().setBundle(__TestBase.Bundle1).build();
      geo = Geo.newBuilder().setCountry("usa").setRegion("ca").build();
      device = Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1).setGeo(geo).build();
      br = BidRequest.newBuilder().setId("bidid").setDevice(device).setApp(app).build();
      BidLog.Builder bl = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
          .setReceivedAt(12345L).setProcessedAt(123456L).setBidRequest(br);
      assertTrue(BidLogUtils.isValid(valid1 = bl.build()));
      bl.setBidResult(BidResult.NO_CANDIDATE).setBidPrice(0)
          .setBidRequest(br.toBuilder().setDevice(device.toBuilder().setOs("ios")));
      assertTrue(BidLogUtils.isValid(valid2 = bl.build()));
    }

    // Test cases with invalid samples.
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidResult(BidResult.UNKNOWN_RESULT).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setBidResult(BidResult.UNKNOWN_RESULT).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidPrice(0).build()));
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setBidPrice(-1).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setBidPrice(1).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setBidPrice(-1).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setReceivedAt(0).build()));
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setReceivedAt(-1).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setReceivedAt(0).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setReceivedAt(-1).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setProcessedAt(1234L).build()));
    assertFalse(BidLogUtils.isValid(valid1.toBuilder().setProcessedAt(12345L).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setProcessedAt(1234L).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder().setProcessedAt(12345L).build()));

    assertFalse(BidLogUtils
        .isValid(valid1.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().setOs("iios"))).build()));
    assertFalse(BidLogUtils
        .isValid(valid1.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().clearOs())).build()));
    assertFalse(BidLogUtils
        .isValid(valid2.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().setOs("iios"))).build()));
    assertFalse(BidLogUtils
        .isValid(valid2.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().clearOs())).build()));

    assertFalse(BidLogUtils
        .isValid(valid1.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().setIfa("v"))).build()));
    assertFalse(BidLogUtils
        .isValid(valid1.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().clearIfa())).build()));
    assertFalse(BidLogUtils
        .isValid(valid2.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().setIfa("v"))).build()));
    assertFalse(BidLogUtils
        .isValid(valid2.toBuilder().setBidRequest(br.toBuilder().setDevice(device.toBuilder().clearIfa())).build()));

    assertFalse(BidLogUtils.isValid(
        valid1.toBuilder().setBidRequest(br.toBuilder().setApp(app.toBuilder().setBundle(" \n \t "))).build()));
    assertFalse(BidLogUtils.isValid(
        valid2.toBuilder().setBidRequest(br.toBuilder().setApp(app.toBuilder().setBundle(" \n \t "))).build()));

    assertFalse(BidLogUtils.isValid(valid1.toBuilder()
        .setBidRequest(br.toBuilder().setDevice(device.toBuilder().setGeo(geo.toBuilder().setCountry(" ")))).build()));
    assertFalse(BidLogUtils.isValid(valid2.toBuilder()
        .setBidRequest(br.toBuilder().setDevice(device.toBuilder().setGeo(geo.toBuilder().setRegion(" \t \n "))))
        .build()));

  }

  @Test
  public void testGetDeviceProfile() {
    BidLog valid1, valid2;
    BidRequest.Builder br;
    Device.Builder device;
    App.Builder app;
    Geo.Builder geo;
    // Test cases with valid samples.
    {
      BidLog.Builder bl = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
          .setReceivedAt(12345L).setProcessedAt(123456L)
          .setBidRequest(br = BidRequest.newBuilder().setId("bidid")
              .setDevice(device = Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                  .setGeo(geo = Geo.newBuilder().setCountry("usa").setRegion("ca")))
              .setApp(app = App.newBuilder().setBundle(__TestBase.Bundle1)));
      assertTrue(BidLogUtils.isValid(valid1 = bl.build()));
      bl.setBidResult(BidResult.NO_CANDIDATE).setBidPrice(0);
      assertTrue(BidLogUtils.isValid(valid2 = bl.build()));
    }
    // check dp1.
    {
      DeviceProfile dp1 = BidLogUtils.getDeviceProfile(valid1);
      // System.out.format("%s\n", ProtoUtils.getJsonFromMessage(dp1)); // <- useful for debugging.
      assertEquals(OsType.ANDROID, dp1.getDeviceId().getOs());
      assertEquals(__TestBase.UUID1.toLowerCase(), dp1.getDeviceId().getUuid().toLowerCase());
      assertEquals(1, dp1.getAppCount());
      assertEquals(__TestBase.Bundle1, dp1.getApp(0).getBundle());
      assertEquals(12345L, dp1.getApp(0).getFirstAt());
      assertEquals(12345L, dp1.getApp(0).getLastAt());
      assertEquals(1, dp1.getApp(0).getCountPerExchangeCount());
      assertEquals(1, dp1.getApp(0).getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
      assertEquals(12345L, dp1.getFirstAt());
      assertEquals(12345L, dp1.getLastAt());
      assertEquals(1, dp1.getGeoCount());
      assertEquals(GeoActivity.newBuilder().setCountry("usa").setRegion("ca").build(), dp1.getGeo(0));
      assertTrue(DeviceProfileUtils.isDpValid(dp1));
    }
    // check dp2.
    {
      DeviceProfile dp2 = BidLogUtils.getDeviceProfile(valid2);
      // System.out.format("%s\n", ProtoUtils.getJsonFromMessage(dp2)); // <- useful for debugging.
      assertEquals(OsType.ANDROID, dp2.getDeviceId().getOs());
      assertEquals(__TestBase.UUID1.toLowerCase(), dp2.getDeviceId().getUuid().toLowerCase());
      assertEquals(1, dp2.getAppCount());
      assertEquals(__TestBase.Bundle1, dp2.getApp(0).getBundle());
      assertEquals(12345L, dp2.getApp(0).getFirstAt());
      assertEquals(12345L, dp2.getApp(0).getLastAt());
      assertEquals(1, dp2.getApp(0).getCountPerExchangeCount());
      assertEquals(1, dp2.getApp(0).getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
      assertEquals(12345L, dp2.getFirstAt());
      assertEquals(12345L, dp2.getLastAt());
      assertEquals(1, dp2.getGeoCount());
      assertEquals(GeoActivity.newBuilder().setCountry("usa").setRegion("ca").build(), dp2.getGeo(0));
      assertTrue(DeviceProfileUtils.isDpValid(dp2));
    }
  }
}
