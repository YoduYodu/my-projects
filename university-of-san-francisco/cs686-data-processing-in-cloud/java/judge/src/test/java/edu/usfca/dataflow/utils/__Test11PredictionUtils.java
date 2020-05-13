package edu.usfca.dataflow.utils;

import static edu.usfca.dataflow.__TestBase.Bundle1;
import static edu.usfca.dataflow.__TestBase.Bundle2;
import static edu.usfca.dataflow.__TestBase.Bundle3;
import static edu.usfca.dataflow.__TestBase.Bundle4;
import static edu.usfca.dataflow.__TestBase.Bundle5;
import static edu.usfca.dataflow.__TestBase.UUID1;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;
import edu.usfca.protobuf.Profile.InAppPurchaseProfile;

/**
 * Note: Unless you modified the provided code in PredictionUtils, these unit tests should pass.
 *
 * (After you update "LOCAL_PATH_TO_RESOURCE_DIR" in Main.)
 */
public class __Test11PredictionUtils {

  static final float EPS = 0.0001f;
  static final int I0 = 486;
  static final int I1 = 686;
  static final int I2 = 586;
  static final int I3 = 777;

  @Test
  public void test1() {
    // IAPP map with 3 bundles: 1, 3, 4.
    Map<String, InAppPurchaseProfile> iappMap = new ImmutableMap.Builder<String, InAppPurchaseProfile>()//
        .put(Bundle1,
            InAppPurchaseProfile.newBuilder().setBundle(Bundle1).setNumPurchasers(12L).setTotalAmount(1000L).build())//
        .put(Bundle3,
            InAppPurchaseProfile.newBuilder().setBundle(Bundle3).setNumPurchasers(6L).setTotalAmount(5000L).build())//
        .put(Bundle4,
            InAppPurchaseProfile.newBuilder().setBundle(Bundle4).setNumPurchasers(1L).setTotalAmount(3000L).build())//
        .build();

    // A device with 4 bundles: 1, 2, 3, and 5.
    DeviceProfile.Builder dp1 = DeviceProfile.newBuilder()
        .setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1))
        .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle5).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    // Expected values: 2/4 (=0.5), 2/5 (=0.4), 18/20 (=0.9), 6000/9001 (=0.666592601).
    float[] actual1 = PredictionUtils.getInputFeatures(dp1.build(), iappMap);
    assertEquals(784, actual1.length);
    assertEquals(0.5f, actual1[I0], EPS);
    assertEquals(0.4f, actual1[I1], EPS);
    assertEquals(0.9f, actual1[I2], EPS);
    assertEquals(0.666592601f, actual1[I3], EPS);
  }

  @Test
  public void test2() {
    // IAPP map with 3 bundles: 1, 3, 4.
    Map<String, InAppPurchaseProfile> iappMap = new ImmutableMap.Builder<String, InAppPurchaseProfile>()//
        .put(Bundle1,
            InAppPurchaseProfile.newBuilder().setBundle(Bundle1).setNumPurchasers(12L).setTotalAmount(1000L).build())//
        .put(Bundle3,
            InAppPurchaseProfile.newBuilder().setBundle(Bundle3).setNumPurchasers(6L).setTotalAmount(5000L).build())//
        .put(Bundle4,
            InAppPurchaseProfile.newBuilder().setBundle(Bundle4).setNumPurchasers(1L).setTotalAmount(3000L).build())//
        .build();

    // A device with 3 bundles: 2, 3, and 4.
    DeviceProfile.Builder dp2 = DeviceProfile.newBuilder()
        .setDeviceId(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1))
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    // Expected values: 2/4 (=0.5), 2/4 (=0.4), 7/20 (=0.35), 8000/9001 (=0.888790134).
    float[] actual2 = PredictionUtils.getInputFeatures(dp2.build(), iappMap);
    assertEquals(784, actual2.length);
    assertEquals(0.5f, actual2[I0], EPS);
    assertEquals(0.5f, actual2[I1], EPS);
    assertEquals(0.35f, actual2[I2], EPS);
    assertEquals(0.888790134f, actual2[I3], EPS);
  }

  @Test
  public void test3() {
    // IAPP map is empty. Nobody bought anything! with 3 bundles: 1, 3, 4.
    Map<String, InAppPurchaseProfile> iappMap = new ImmutableMap.Builder<String, InAppPurchaseProfile>().build();

    // A device with 3 bundles: 2, 3, and 4.
    DeviceProfile.Builder dp2 = DeviceProfile.newBuilder()
        .setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1))
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    // Expected values: 0's.
    float[] actual3 = PredictionUtils.getInputFeatures(dp2.build(), iappMap);
    assertEquals(784, actual3.length);
    assertEquals(0.f, actual3[I0], EPS);
    assertEquals(0.f, actual3[I1], EPS);
    assertEquals(0.f, actual3[I2], EPS);
    assertEquals(0.f, actual3[I3], EPS);
  }
}
