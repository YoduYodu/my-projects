package edu.usfca.dataflow.jobs2;

import static edu.usfca.dataflow.__TestBase.Bundle1;
import static edu.usfca.dataflow.__TestBase.Bundle2;
import static edu.usfca.dataflow.__TestBase.Bundle3;
import static edu.usfca.dataflow.__TestBase.Bundle4;
import static edu.usfca.dataflow.__TestBase.Bundle5;
import static edu.usfca.dataflow.__TestBase.UUID10;
import static edu.usfca.dataflow.__TestBase.UUID3;
import static edu.usfca.dataflow.__TestBase.UUID4;
import static edu.usfca.dataflow.__TestBase.UUID5;
import static edu.usfca.dataflow.__TestBase.UUID6;
import static edu.usfca.dataflow.__TestBase.UUID7;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.beam.sdk.Pipeline.PipelineExecutionException;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import edu.usfca.dataflow.CorruptedDataException;
import edu.usfca.dataflow.transforms.Features.GetInputToModel;
import edu.usfca.dataflow.utils.DeviceProfileUtils;
import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;
import edu.usfca.protobuf.Profile.InAppPurchaseProfile;

public class __Test13PredictionJob {
  @Rule
  public Timeout timeout = Timeout.millis(10000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  static final float EPS = 0.0001f;
  static final int I0 = 486;
  static final int I1 = 686;
  static final int I2 = 586;
  static final int I3 = 777;

  @Test
  public void testWithDefaultInstances() {
    PCollection<String> dpBase64 = tp.apply(Create.of(DeviceProfile.getDefaultInstance()))
        .apply(MapElements.into(TypeDescriptors.strings()).via((DeviceProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> suspiciousBase64 = tp.apply(Create.of(DeviceId.getDefaultInstance()))
        .apply(MapElements.into(TypeDescriptors.strings()).via((DeviceId x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> iappBase64 = tp.apply(Create.of(InAppPurchaseProfile.getDefaultInstance())).apply(
        MapElements.into(TypeDescriptors.strings()).via((InAppPurchaseProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PAssert.that(PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel()))
        .empty();

    tp.run();
  }

  @Test
  public void test00Duplicates1() {

    // A device with 4 bundles: 1, 2, 3, and 5.
    DeviceId id1 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID10.toUpperCase()).build();
    DeviceId id2 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID10.toLowerCase()).build();
    DeviceProfile.Builder dp1 = DeviceProfile.newBuilder().setDeviceId(id1)
        .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle5).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    PCollection<String> dpBase64 = tp.apply(Create.of(dp1.build(), dp1.setDeviceId(id2).build()))
        .apply(MapElements.into(TypeDescriptors.strings()).via((DeviceProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> suspiciousBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    PCollection<String> iappBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    try {
      PAssert.that(PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel()))
          .satisfies(out -> {
            return null;
          });

      tp.run();
      System.err.println("CorruptedDataException was expected, due to duplicate Device IDs.");
      fail();
    } catch (PipelineExecutionException e) {
      assertTrue(e.getCause() instanceof CorruptedDataException);
    }
  }

  @Test
  public void test00Duplicates2() {

    // A device with 4 bundles: 1, 2, 3, and 5.
    DeviceId id1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID10.toUpperCase()).build();
    DeviceId id2 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID10.toLowerCase()).build();
    DeviceProfile.Builder dp1 = DeviceProfile.newBuilder().setDeviceId(id1)
        .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle5).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    PCollection<String> dpBase64 = tp.apply(Create.of(dp1.build(), dp1.setDeviceId(id2).build()))
        .apply(MapElements.into(TypeDescriptors.strings()).via((DeviceProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> suspiciousBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    PCollection<String> iappBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    try {
      PAssert.that(PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel()))
          .satisfies(out -> {
            return null;
          });

      tp.run();
      System.err.println("CorruptedDataException was expected, due to duplicate Device IDs.");
      fail();
    } catch (PipelineExecutionException e) {
      assertTrue(e.getCause() instanceof CorruptedDataException);
    }
  }

  @Test
  public void test01WithoutSuspiciousUsers() {
    // This is the same test as in "__Test12..." class (in terms of the example data being used).

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
    DeviceId id1 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID10).build();
    DeviceProfile.Builder dp1 = DeviceProfile.newBuilder().setDeviceId(id1)
        .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle5).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    PCollection<String> dpBase64 = tp.apply(Create.of(dp1.build()))
        .apply(MapElements.into(TypeDescriptors.strings()).via((DeviceProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> suspiciousBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    PCollection<String> iappBase64 = tp.apply(Create.of(iappMap.values().stream().collect(Collectors.toSet()))).apply(
        MapElements.into(TypeDescriptors.strings()).via((InAppPurchaseProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PAssert.that(PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel()))
        .satisfies(out -> {
          assertEquals(1, Iterables.size(out));
          KV<DeviceId, float[]> actual = out.iterator().next();
          assertEquals(id1, DeviceProfileUtils.getCanonicalId(actual.getKey()));
          assertEquals(784, actual.getValue().length);
          assertEquals(0.5f, actual.getValue()[I0], EPS);
          assertEquals(0.4f, actual.getValue()[I1], EPS);
          assertEquals(0.9f, actual.getValue()[I2], EPS);
          assertEquals(0.666592601f, actual.getValue()[I3], EPS);
          return null;
        });

    tp.run();
  }

  @Test
  public void test02WithoutSuspiciousUsers() {
    // This is the same test as in "__Test12..." class (in terms of the example data being used).

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
    DeviceId id2 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID10).build();
    DeviceProfile.Builder dp2 = DeviceProfile.newBuilder().setDeviceId(id2)
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    PCollection<String> dpBase64 = tp.apply(Create.of(dp2.build()))
        .apply(MapElements.into(TypeDescriptors.strings()).via((DeviceProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> suspiciousBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    PCollection<String> iappBase64 = tp.apply(Create.of(iappMap.values().stream().collect(Collectors.toSet()))).apply(
        MapElements.into(TypeDescriptors.strings()).via((InAppPurchaseProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PAssert.that(PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel()))
        .satisfies(out -> {
          assertEquals(1, Iterables.size(out));
          KV<DeviceId, float[]> actual = out.iterator().next();
          assertEquals(id2, DeviceProfileUtils.getCanonicalId(actual.getKey()));
          assertEquals(784, actual.getValue().length);
          assertEquals(0.5f, actual.getValue()[I0], EPS);
          assertEquals(0.5f, actual.getValue()[I1], EPS);
          assertEquals(0.35f, actual.getValue()[I2], EPS);
          assertEquals(0.888790134f, actual.getValue()[I3], EPS);
          return null;
        });

    tp.run();
  }

  @Test
  public void test03DeviceIdCheck() {
    // Same test as "test01" above, but with different DeviceId.
    // Note that your "GetInputToModel" must filter out device profiles based on device ID's uuid.
    // TODO: If your code fails this test, make sure you double-check the filtering logic in your "GetInputToModel"
    // class.

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
    DeviceId id1 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID3).build();
    DeviceId id2 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID4).build();
    DeviceId id3 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID5).build();
    DeviceId id4 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID6).build();
    DeviceId id5 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID7).build();
    DeviceProfile.Builder dp1 = DeviceProfile.newBuilder().setDeviceId(id1)
        .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.CS_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.USF_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .addApp(AppActivity.newBuilder().setBundle(Bundle5).setFirstAt(1234L).setLastAt(1234L)
            .putCountPerExchange(Exchange.ADX_VALUE, 1).build())//
        .setFirstAt(1234L).setLastAt(1234L).addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build());

    PCollection<String> dpBase64 = tp.apply(Create.of(dp1.build(), dp1.setDeviceId(id2).build(), //
        dp1.setDeviceId(id3).build(), //
        dp1.setDeviceId(id4).build(), //
        dp1.setDeviceId(id5).build()//
    )).apply(MapElements.into(TypeDescriptors.strings()).via((DeviceProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PCollection<String> suspiciousBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    PCollection<String> iappBase64 = tp.apply(Create.of(iappMap.values().stream().collect(Collectors.toSet()))).apply(
        MapElements.into(TypeDescriptors.strings()).via((InAppPurchaseProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    PAssert.that(PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel()))
        .empty();

    tp.run();
  }

}
