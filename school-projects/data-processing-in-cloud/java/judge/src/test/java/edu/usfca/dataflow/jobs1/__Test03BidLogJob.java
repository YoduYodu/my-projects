package edu.usfca.dataflow.jobs1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.Iterables;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.App;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Geo;

import edu.usfca.dataflow.__TestBase;
import edu.usfca.dataflow.jobs1.BidLogJob.BidLog2DeviceProfile;
import edu.usfca.dataflow.utils.DeviceProfileUtils;
import edu.usfca.protobuf.Bid.BidLog;
import edu.usfca.protobuf.Bid.BidResult;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;

public class __Test03BidLogJob {
  @Rule
  public Timeout timeout = Timeout.millis(5000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  // Invalid samples.
  static List<BidLog> getInvalidBidLogs() {
    BidLog.Builder bl = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
        .setReceivedAt(12345L).setProcessedAt(123456L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)).build());
    BidLog valid = bl.build();

    return Arrays.asList(BidLog.getDefaultInstance(), valid.toBuilder().setExchange(Exchange.UNKNOWN_EXCHANGE).build(),
        valid.toBuilder().setBidPrice(-1).build(), valid.toBuilder().setReceivedAt(-1).build(),
        valid.toBuilder().setProcessedAt(1234L).build());
  }

  // Samples including valid/invalid bidlogs.
  static List<BidLog> getBidLogs_SetA() {
    List<BidLog> bls = new ArrayList<>();

    bls.add(BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
        .setReceivedAt(12345L).setProcessedAt(123456L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid1")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)))
        .build());

    bls.add(BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(234)
        .setReceivedAt(1234L).setProcessedAt(12345L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid2")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("USA").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle3)))
        .build());

    bls.add(BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.BID).setBidPrice(567)
        .setReceivedAt(123L).setProcessedAt(1234567L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid3")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("nv")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)))
        .build());

    // invalid
    bls.add(BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.BID).setReceivedAt(1234L)
        .setProcessedAt(12345L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid3")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle2)))
        .build());

    // invalid
    bls.add(BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.NO_CANDIDATE).setReceivedAt(123456L)
        .setProcessedAt(1234567L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid3")
            .setDevice(Device.newBuilder().setOs("Androi").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle2)))
        .build());

    return bls;
  }

  @Test
  public void testProfileFromBidLog01() {
    // Given invalid BidLogs as input, the expected output is an empty PCollection (as those need to be filtered out).
    PCollection<BidLog> bls = tp.apply(Create.of(getInvalidBidLogs()));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).empty();
    tp.run();
  }

  @Test
  public void testProfileFromBidLog02() {
    // In the samples of "SetA", you find three valid BidLogs and two invalid BidLogs.
    // The three BidLogs are for the same DeviceId (Android and UUID1), and thus the expected output is 1 DeviceProfile.
    PCollection<BidLog> bls = tp.apply(Create.of(getBidLogs_SetA()));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          assertEquals(1, Iterables.size(out));
          DeviceProfile actual = out.iterator().next();
          assertTrue(DeviceProfileUtils.isDpValid(actual));

          // This may help you debug your code.
          // System.out.println(actual.toString());

          assertEquals(OsType.ANDROID, actual.getDeviceId().getOs());
          assertTrue(__TestBase.UUID1.equalsIgnoreCase(actual.getDeviceId().getUuid()));

          assertEquals(123L, actual.getFirstAt());
          assertEquals(12345L, actual.getLastAt());

          // apps
          assertEquals(2, actual.getAppCount());

          return null;
        });
    tp.run();
  }

  @Test
  public void testProfileFromBidLog03() {
    // Let's begin with one valid BidLog.
    BidLog.Builder builder = BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID)
        .setBidPrice(123).setReceivedAt(12345L).setProcessedAt(123456L)
        .setBidRequest(BidRequest.newBuilder().setId("bidid1")
            .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1)
                .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
            .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)));
    BidLog bl1 = builder.build();
    builder.getBidRequestBuilder().getDeviceBuilder().setIfa(__TestBase.UUID1.toUpperCase());
    BidLog bl2 = builder.build();
    builder.getBidRequestBuilder().getDeviceBuilder().setOs("ios");
    BidLog bl3 = builder.build();

    PAssert.that(tp.apply(Create.of(bl1, bl2, bl3))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          // Given three valid BidLogs with two unique DeviceIDs, you'd expect to see two DeviceProfiles as output.
          assertEquals(2, Iterables.size(out));
          Iterator<DeviceProfile> it = out.iterator();
          DeviceProfile dp1 = it.next();
          DeviceProfile dp2 = it.next();
          assertTrue(__TestBase.UUID1.equalsIgnoreCase(dp1.getDeviceId().getUuid()));
          assertTrue(__TestBase.UUID1.equalsIgnoreCase(dp2.getDeviceId().getUuid()));

          assertTrue(dp1.getDeviceId().getOs() != dp2.getDeviceId().getOs());
          assertTrue(dp1.getDeviceId().getOs() == OsType.IOS || dp1.getDeviceId().getOs() == OsType.ANDROID);
          assertTrue(dp2.getDeviceId().getOs() == OsType.IOS || dp2.getDeviceId().getOs() == OsType.ANDROID);

          assertTrue(DeviceProfileUtils.isDpValid(dp1));
          assertTrue(DeviceProfileUtils.isDpValid(dp2));
          return null;
        });
    tp.run();
  }

  @Test
  public void testProfileFromBidLog04() {
    PCollection<BidLog> bls = tp.apply(Create.of(getBidLogs_SetA()));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          assertEquals(1, Iterables.size(out));
          DeviceProfile actual = out.iterator().next();

          assertTrue(DeviceProfileUtils.isDpValid(actual));
          // This may help you debug your code.
          // System.out.println(actual.toString());

          assertEquals(OsType.ANDROID, actual.getDeviceId().getOs());
          assertEquals(__TestBase.UUID1.toLowerCase(), actual.getDeviceId().getUuid().toLowerCase());

          assertEquals(123L, actual.getFirstAt());
          assertEquals(12345L, actual.getLastAt());

          // apps
          assertEquals(2, actual.getAppCount());
          AppActivity app1 = actual.getApp(0);
          AppActivity app2 = actual.getApp(1);
          assertTrue((__TestBase.Bundle1.equals(app1.getBundle()) && __TestBase.Bundle3.equals(app2.getBundle()))
              || (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())));
          if (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())) {
            app1 = actual.getApp(1);
            app2 = actual.getApp(0);
          }
          // bundle 1 = app1
          assertEquals(__TestBase.Bundle1, app1.getBundle());
          assertEquals(123L, app1.getFirstAt());
          assertEquals(12345L, app1.getLastAt());
          assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
          assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

          // bundle 3 = app2
          assertEquals(__TestBase.Bundle3, app2.getBundle());
          assertEquals(1234L, app2.getFirstAt());
          assertEquals(1234L, app2.getLastAt());
          assertEquals(1, app2.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

          assertEquals(3, actual.getGeoCount());
          Set<KV<String, String>> geos = new HashSet<>();
          for (int i = 0; i < 3; i++) {
            final String country = actual.getGeo(i).getCountry();
            final String region = actual.getGeo(i).getRegion();
            geos.add(KV.of(country, region));
          }
          assertTrue(geos.contains(KV.of("usa", "ca")));
          assertTrue(geos.contains(KV.of("USA", "ca")));
          assertTrue(geos.contains(KV.of("usa", "nv")));

          return null;
        });
    tp.run();
  }

  @Test
  public void testProfileFromBidLog05() {
    List<BidLog> bidLogs = getBidLogs_SetA();
    bidLogs.addAll(Arrays.asList(
        BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
            .setReceivedAt(12345L).setProcessedAt(123456L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid1")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1.toUpperCase())
                    .setGeo(Geo.newBuilder().setCountry("Usa").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle1.toUpperCase())))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(234).setReceivedAt(1234L)
            .setProcessedAt(12345L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid2")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1.toUpperCase())
                    .setGeo(Geo.newBuilder().setCountry("USA").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle3.toUpperCase())))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.BID).setBidPrice(567)
            .setReceivedAt(123L).setProcessedAt(1234567L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid3")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID1.toUpperCase())
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("nv")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle1.toUpperCase())))
            .build()));

    PCollection<BidLog> bls = tp.apply(Create.of(bidLogs));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          assertEquals(1, Iterables.size(out)); // uuid is case-insensitive
          DeviceProfile actual = out.iterator().next();
          assertTrue(DeviceProfileUtils.isDpValid(actual));

          assertEquals(OsType.ANDROID, actual.getDeviceId().getOs());
          assertEquals(__TestBase.UUID1.toLowerCase(), actual.getDeviceId().getUuid().toLowerCase());

          assertEquals(123L, actual.getFirstAt());
          assertEquals(12345L, actual.getLastAt());

          // apps
          assertEquals(4, actual.getAppCount());
          Map<String, AppActivity> apps =
              new HashMap<>(actual.getAppList().stream().collect(Collectors.toMap(x -> x.getBundle(), x -> x)));

          assertTrue(apps.keySet().containsAll(Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle3,
              __TestBase.Bundle3.toUpperCase(), __TestBase.Bundle1.toUpperCase())));
          // bundle1
          AppActivity app = apps.get(__TestBase.Bundle1);
          assertEquals(__TestBase.Bundle1, app.getBundle());
          assertEquals(123L, app.getFirstAt());
          assertEquals(12345L, app.getLastAt());
          assertEquals(1, app.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
          assertEquals(1, app.getCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

          // bundle1 - upper (bunldes are case sensitive)
          app = apps.get(__TestBase.Bundle1.toUpperCase());
          assertEquals(__TestBase.Bundle1.toUpperCase(), app.getBundle());
          assertEquals(123L, app.getFirstAt());
          assertEquals(12345L, app.getLastAt());
          assertEquals(1, app.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
          assertEquals(1, app.getCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

          // bundle 3
          app = apps.get(__TestBase.Bundle3);
          assertEquals(__TestBase.Bundle3, app.getBundle());
          assertEquals(1234L, app.getFirstAt());
          assertEquals(1234L, app.getLastAt());
          assertEquals(1, app.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

          // bundle 3 - upper
          app = apps.get(__TestBase.Bundle3.toUpperCase());
          assertEquals(__TestBase.Bundle3.toUpperCase(), app.getBundle());
          assertEquals(1234L, app.getFirstAt());
          assertEquals(1234L, app.getLastAt());
          assertEquals(1, app.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

          assertEquals(4, actual.getGeoCount());
          Set<KV<String, String>> geos = new HashSet<>();
          for (int i = 0; i < 4; i++) {
            final String country = actual.getGeo(i).getCountry();
            final String region = actual.getGeo(i).getRegion();
            geos.add(KV.of(country, region));
          }
          assertTrue(geos.contains(KV.of("usa", "ca")));
          assertTrue(geos.contains(KV.of("Usa", "ca")));
          assertTrue(geos.contains(KV.of("USA", "ca")));
          assertTrue(geos.contains(KV.of("usa", "nv")));

          return null;
        });
    tp.run();
  }

  @Test
  public void testProfileFromBidLog06() {
    List<BidLog> bidLogs = getBidLogs_SetA();
    bidLogs.addAll(Arrays.asList(
        BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
            .setReceivedAt(12345L).setProcessedAt(123456L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid1")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID2.toLowerCase())
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(234).setReceivedAt(1234L)
            .setProcessedAt(12345L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid2")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID2.toUpperCase())
                    .setGeo(Geo.newBuilder().setCountry("USA").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle3)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.BID).setBidPrice(567)
            .setReceivedAt(123L).setProcessedAt(1234567L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid3")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID2.toLowerCase())
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("nv")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)))
            .build()));

    PCollection<BidLog> bls = tp.apply(Create.of(bidLogs));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          assertEquals(2, Iterables.size(out)); // uuid is case-insensitive
          boolean uuid1 = false, uuid2 = false;
          for (DeviceProfile actual : out) {
            assertTrue(DeviceProfileUtils.isDpValid(actual));
            assertEquals(OsType.ANDROID, actual.getDeviceId().getOs());
            uuid1 |= StringUtils.equalsIgnoreCase(__TestBase.UUID1.toLowerCase(),
                actual.getDeviceId().getUuid().toLowerCase());
            uuid2 |= StringUtils.equalsIgnoreCase(__TestBase.UUID2.toLowerCase(),
                actual.getDeviceId().getUuid().toLowerCase());

            assertEquals(123L, actual.getFirstAt());
            assertEquals(12345L, actual.getLastAt());

            // apps
            assertEquals(2, actual.getAppCount());
            AppActivity app1 = actual.getApp(0);
            AppActivity app2 = actual.getApp(1);
            assertTrue((__TestBase.Bundle1.equals(app1.getBundle()) && __TestBase.Bundle3.equals(app2.getBundle()))
                || (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())));
            if (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())) {
              app1 = actual.getApp(1);
              app2 = actual.getApp(0);
            }
            // bundle 1 = app1
            assertEquals(__TestBase.Bundle1, app1.getBundle());
            assertEquals(123L, app1.getFirstAt());
            assertEquals(12345L, app1.getLastAt());
            assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
            assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

            // bundle 3 = app2
            assertEquals(__TestBase.Bundle3, app2.getBundle());
            assertEquals(1234L, app2.getFirstAt());
            assertEquals(1234L, app2.getLastAt());
            assertEquals(1, app2.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

            assertEquals(3, actual.getGeoCount());
            Set<KV<String, String>> geos = new HashSet<>();
            for (int i = 0; i < 3; i++) {
              final String country = actual.getGeo(i).getCountry();
              final String region = actual.getGeo(i).getRegion();
              geos.add(KV.of(country, region));
            }
            assertTrue(geos.contains(KV.of("usa", "ca")));
            assertTrue(geos.contains(KV.of("USA", "ca")));
            assertTrue(geos.contains(KV.of("usa", "nv")));
          }
          assertTrue(uuid1 && uuid2);
          return null;
        });
    tp.run();
  }

  @Test
  public void testProfileFromBidLog07() {
    List<BidLog> bidLogs = getBidLogs_SetA();
    bidLogs.addAll(Arrays.asList(
        BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
            .setReceivedAt(12345L).setProcessedAt(123456L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid1")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID3.toLowerCase())
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle2)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(99).setReceivedAt(12L)
            .setProcessedAt(12345L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid2")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID3.toUpperCase())
                    .setGeo(Geo.newBuilder().setCountry("uSa").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle3)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(0).setReceivedAt(1234L)
            .setProcessedAt(12345L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid2")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID3.toLowerCase())
                    .setGeo(Geo.newBuilder().setCountry("USA").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle3)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.BID).setBidPrice(567)
            .setReceivedAt(123L).setProcessedAt(1234567L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid3")
                .setDevice(Device.newBuilder().setOs("And").setIfa(__TestBase.UUID3)
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("nv")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)))
            .build()));

    PCollection<BidLog> bls = tp.apply(Create.of(bidLogs));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          assertEquals(2, Iterables.size(out)); // uuid is case-insensitive!!
          boolean uuid1 = false, uuid2 = false;
          for (DeviceProfile actual : out) {
            assertTrue(DeviceProfileUtils.isDpValid(actual));
            assertEquals(OsType.ANDROID, actual.getDeviceId().getOs());
            if (StringUtils.equalsIgnoreCase(__TestBase.UUID1, actual.getDeviceId().getUuid())) {
              uuid1 = true;
              assertEquals(123L, actual.getFirstAt());
              assertEquals(12345L, actual.getLastAt());

              // apps
              assertEquals(2, actual.getAppCount());
              AppActivity app1 = actual.getApp(0);
              AppActivity app2 = actual.getApp(1);
              assertTrue((__TestBase.Bundle1.equals(app1.getBundle()) && __TestBase.Bundle3.equals(app2.getBundle()))
                  || (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())));
              if (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())) {
                app1 = actual.getApp(1);
                app2 = actual.getApp(0);
              }
              // bundle 1 = app1
              assertEquals(__TestBase.Bundle1, app1.getBundle());
              assertEquals(123L, app1.getFirstAt());
              assertEquals(12345L, app1.getLastAt());
              assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
              assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

              // bundle 3 = app2
              assertEquals(__TestBase.Bundle3, app2.getBundle());
              assertEquals(1234L, app2.getFirstAt());
              assertEquals(1234L, app2.getLastAt());
              assertEquals(1, app2.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

              assertEquals(3, actual.getGeoCount());
              Set<KV<String, String>> geos = new HashSet<>();
              for (int i = 0; i < 3; i++) {
                final String country = actual.getGeo(i).getCountry();
                final String region = actual.getGeo(i).getRegion();
                geos.add(KV.of(country, region));
              }
              assertTrue(geos.contains(KV.of("usa", "ca")));
              assertTrue(geos.contains(KV.of("USA", "ca")));
              assertTrue(geos.contains(KV.of("usa", "nv")));
            } else if (StringUtils.equalsIgnoreCase(__TestBase.UUID3, actual.getDeviceId().getUuid())) {
              uuid2 = true;
              assertEquals(12L, actual.getFirstAt());
              assertEquals(12345L, actual.getLastAt());

              // apps
              assertEquals(2, actual.getAppCount());
              AppActivity app1 = actual.getApp(0);
              AppActivity app2 = actual.getApp(1);
              assertTrue((__TestBase.Bundle2.equals(app1.getBundle()) && __TestBase.Bundle3.equals(app2.getBundle()))
                  || (__TestBase.Bundle2.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())));
              if (__TestBase.Bundle2.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())) {
                app1 = actual.getApp(1);
                app2 = actual.getApp(0);
              }
              // bundle 2 = app1
              assertEquals(__TestBase.Bundle2, app1.getBundle());
              assertEquals(12345L, app1.getFirstAt());
              assertEquals(12345L, app1.getLastAt());
              assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));

              // bundle 3 = app2
              assertEquals(__TestBase.Bundle3, app2.getBundle());
              assertEquals(12L, app2.getFirstAt());
              assertEquals(12L, app2.getLastAt());
              assertEquals(1, app2.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

              assertEquals(2, actual.getGeoCount());
              Set<KV<String, String>> geos = new HashSet<>();
              for (int i = 0; i < 2; i++) {
                final String country = actual.getGeo(i).getCountry();
                final String region = actual.getGeo(i).getRegion();
                geos.add(KV.of(country, region));
              }
              assertTrue(geos.contains(KV.of("usa", "ca")));
              assertTrue(geos.contains(KV.of("uSa", "ca")));
            } else {

            }
          }
          assertTrue(uuid1 && uuid2);
          return null;
        });
    tp.run();
  }

  @Test
  public void testProfileFromBidLog08() {
    List<BidLog> bidLogs = getBidLogs_SetA();
    bidLogs.addAll(bidLogs);
    bidLogs.addAll(Arrays.asList(
        BidLog.newBuilder().setExchange(Exchange.OPENX).setBidResult(BidResult.BID).setBidPrice(123)
            .setReceivedAt(12345L).setProcessedAt(123456L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid1")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID3)
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle2)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(99).setReceivedAt(12L)
            .setProcessedAt(12345L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid2")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID3)
                    .setGeo(Geo.newBuilder().setCountry("uSa").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle3)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.ADX).setBidResult(BidResult.BID).setBidPrice(0).setReceivedAt(1234L)
            .setProcessedAt(12345L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid2")
                .setDevice(Device.newBuilder().setOs("Android").setIfa(__TestBase.UUID3)
                    .setGeo(Geo.newBuilder().setCountry("USA").setRegion("ca")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle3)))
            .build(),

        BidLog.newBuilder().setExchange(Exchange.INMOBI).setBidResult(BidResult.BID).setBidPrice(567)
            .setReceivedAt(123L).setProcessedAt(1234567L)
            .setBidRequest(BidRequest.newBuilder().setId("bidid3")
                .setDevice(Device.newBuilder().setOs("And").setIfa(__TestBase.UUID3)
                    .setGeo(Geo.newBuilder().setCountry("usa").setRegion("nv")))
                .setApp(App.newBuilder().setBundle(__TestBase.Bundle1)))
            .build()));

    PCollection<BidLog> bls = tp.apply(Create.of(bidLogs));
    PAssert.that(bls.apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()))
        .apply(new BidLog2DeviceProfile())).satisfies(out -> {
          assertEquals(2, Iterables.size(out)); // uuid is case-insensitive
          boolean uuid1 = false, uuid2 = false;
          for (DeviceProfile actual : out) {
            assertTrue(DeviceProfileUtils.isDpValid(actual));
            assertEquals(OsType.ANDROID, actual.getDeviceId().getOs());
            if (StringUtils.equalsIgnoreCase(__TestBase.UUID1, actual.getDeviceId().getUuid())) {
              uuid1 = true;
              assertEquals(123L, actual.getFirstAt());
              assertEquals(12345L, actual.getLastAt());

              // apps
              assertEquals(2, actual.getAppCount());
              AppActivity app1 = actual.getApp(0);
              AppActivity app2 = actual.getApp(1);
              assertTrue((__TestBase.Bundle1.equals(app1.getBundle()) && __TestBase.Bundle3.equals(app2.getBundle()))
                  || (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())));
              if (__TestBase.Bundle1.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())) {
                app1 = actual.getApp(1);
                app2 = actual.getApp(0);
              }
              // bundle 1 = app1
              assertEquals(__TestBase.Bundle1, app1.getBundle());
              assertEquals(123L, app1.getFirstAt());
              assertEquals(12345L, app1.getLastAt());
              assertEquals(2, app1.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
              assertEquals(2, app1.getCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

              // bundle 3 = app2
              assertEquals(__TestBase.Bundle3, app2.getBundle());
              assertEquals(1234L, app2.getFirstAt());
              assertEquals(1234L, app2.getLastAt());
              assertEquals(2, app2.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

              assertEquals(3, actual.getGeoCount());
              Set<KV<String, String>> geos = new HashSet<>();
              for (int i = 0; i < 3; i++) {
                final String country = actual.getGeo(i).getCountry();
                final String region = actual.getGeo(i).getRegion();
                geos.add(KV.of(country, region));
              }
              assertTrue(geos.contains(KV.of("usa", "ca")));
              assertTrue(geos.contains(KV.of("USA", "ca")));
              assertTrue(geos.contains(KV.of("usa", "nv")));
            } else if (StringUtils.equalsIgnoreCase(__TestBase.UUID3, actual.getDeviceId().getUuid())) {
              uuid2 = true;
              assertEquals(12L, actual.getFirstAt());
              assertEquals(12345L, actual.getLastAt());

              // apps
              assertEquals(2, actual.getAppCount());
              AppActivity app1 = actual.getApp(0);
              AppActivity app2 = actual.getApp(1);
              assertTrue((__TestBase.Bundle2.equals(app1.getBundle()) && __TestBase.Bundle3.equals(app2.getBundle()))
                  || (__TestBase.Bundle2.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())));
              if (__TestBase.Bundle2.equals(app2.getBundle()) || __TestBase.Bundle3.equals(app1.getBundle())) {
                app1 = actual.getApp(1);
                app2 = actual.getApp(0);
              }
              // bundle 2 = app1
              assertEquals(__TestBase.Bundle2, app1.getBundle());
              assertEquals(12345L, app1.getFirstAt());
              assertEquals(12345L, app1.getLastAt());
              assertEquals(1, app1.getCountPerExchangeOrThrow(Exchange.OPENX_VALUE));

              // bundle 3 = app2
              assertEquals(__TestBase.Bundle3, app2.getBundle());
              assertEquals(12L, app2.getFirstAt());
              assertEquals(12L, app2.getLastAt());
              assertEquals(1, app2.getCountPerExchangeOrThrow(Exchange.ADX_VALUE));

              assertEquals(2, actual.getGeoCount());
              Set<KV<String, String>> geos = new HashSet<>();
              for (int i = 0; i < 2; i++) {
                final String country = actual.getGeo(i).getCountry();
                final String region = actual.getGeo(i).getRegion();
                geos.add(KV.of(country, region));
              }
              assertTrue(geos.contains(KV.of("usa", "ca")));
              assertTrue(geos.contains(KV.of("uSa", "ca")));
            } else {

            }
          }
          assertTrue(uuid1 && uuid2);
          return null;
        });
    tp.run();
  }
}
