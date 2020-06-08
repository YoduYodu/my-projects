package edu.usfca.dataflow.jobs1;

import static edu.usfca.dataflow.__TestBase.UUID1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.Pipeline.PipelineExecutionException;
import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.Iterables;

import edu.usfca.dataflow.CorruptedDataException;
import edu.usfca.dataflow.__TestBase;
import edu.usfca.dataflow.transforms.AppProfiles.ComputeAppProfiles;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;

public class __Test04BidLogJob {
  @Rule
  public Timeout timeout = Timeout.millis(10000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  @Test
  public void testAppProfile_basic01() {
    DeviceId.Builder did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);

    // lifetime
    DeviceProfile.Builder life1 = DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app1).addApp(app2);

    PAssert.that(tp.apply(Create.of(life1.build())).apply(new ComputeAppProfiles())).satisfies(out -> {
      assertEquals(2, Iterables.size(out));

      Map<String, AppProfile> profiles = new HashMap<>();
      for (AppProfile app : out) {
        profiles.put(app.getBundle(), app);
      }
      assertTrue(profiles.keySet().containsAll(Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle2)));

      {
        AppProfile _app = profiles.get(__TestBase.Bundle1);
        assertEquals(1, _app.getUserCount());
        assertEquals(1, _app.getUserCountPerExchangeOrThrow(Exchange.USF_VALUE));
        assertEquals(1, _app.getUserCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));

        // Exchange maps: intentionally not tested.
      }
      {
        AppProfile _app = profiles.get(__TestBase.Bundle2);
        assertEquals(1, _app.getUserCount());
        assertEquals(1, _app.getUserCountPerExchangeOrThrow(Exchange.CS_VALUE));
        assertEquals(1, _app.getUserCountPerExchangeOrThrow(Exchange.USF_VALUE));
      }
      return null;
    });
    tp.run();
  }

  @Test
  public void testAppProfile_basic02() {
    DeviceId.Builder did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1);
    DeviceId.Builder did2 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID2);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);

    // lifetime
    DeviceProfile.Builder life1 = DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app1).addApp(app2);
    DeviceProfile.Builder life2 =
        DeviceProfile.newBuilder().setDeviceId(did2).setFirstAt(15000L).setLastAt(20000L).addGeo(geo1).addApp(app2);

    PAssert.that(tp.apply(Create.of(life1.build(), life2.build())).apply(new ComputeAppProfiles())).satisfies(out -> {
      assertEquals(2, Iterables.size(out));

      Map<String, AppProfile> profiles = new HashMap<>();
      for (AppProfile app : out) {
        profiles.put(app.getBundle(), app);
      }
      assertTrue(profiles.keySet().containsAll(Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle2)));

      {
        AppProfile _app = profiles.get(__TestBase.Bundle1);
        assertEquals(1, _app.getUserCount());
        // Exchange maps: intentionally not tested.
      }
      {
        AppProfile _app = profiles.get(__TestBase.Bundle2);
        assertEquals(2, _app.getUserCount());
        // Exchange maps: intentionally not tested.
      }
      return null;
    });
    tp.run();

  }

  // Given c between 0 and 25 (inclusive), returns an invalid DeviceProfile.
  // For other values, returns a valid DeviceProfile.
  static DeviceProfile getSampleDp(int c) {
    DeviceId.Builder did = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.Bundle1);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");
    GeoActivity.Builder geo2 = GeoActivity.newBuilder().setCountry("usa").setRegion("CA");
    GeoActivity.Builder geo3 = GeoActivity.newBuilder().setCountry("USA").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);
    DeviceProfile.Builder _dp = DeviceProfile.newBuilder().setDeviceId(did).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addGeo(geo2).addGeo(geo3).addApp(app1).addApp(app2);

    DeviceProfile.Builder dp = _dp.build().toBuilder();

    switch (c) {
      case 0:
        return dp.clearDeviceId().build();
      case 1:
        return dp.setDeviceId(did.build().toBuilder().clearOs()).build();
      case 2:
        return dp.setDeviceId(did.build().toBuilder().setOs(OsType.UNKNOWN_OS_TYPE)).build();
      case 3:
        return dp.setDeviceId(did.build().toBuilder().clearUuid()).build();
      case 4:
        return dp.setDeviceId(did.build().toBuilder().setUuid(" ")).build();
      case 5:
        return dp.clearApp().build();
      case 6:
        return dp.removeApp(0).build();
      case 7:
        return dp.removeApp(1).build();
      case 8:
        return dp.addApp(dp.getApp(0)).build();
      case 9:
        return dp.addApp(dp.getApp(1)).build();
      case 10:
        return dp.addApp(AppActivity.newBuilder().setBundle("").setFirstAt(12345L).setLastAt(12345L).build()).build();
      case 11:
        return dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).clearCountPerExchange()).build();
      case 12:
        return dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).putCountPerExchange(10, 1)).build();
      case 13:
        return dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).putCountPerExchange(5, 0)).build();
      case 14:
        return dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).setFirstAt(20001L).setLastAt(20001L))
            .build();
      case 15:
        return dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).setFirstAt(2001L).setLastAt(2001L))
            .build();
      case 16:
        return dp.clearApp().clearFirstAt().build();
      case 17:
        return dp.clearApp().clearLastAt().build();
      case 18:
        return dp.removeGeo(2).build();
      case 19:
        return dp.addGeo(geo1).build();
      case 20:
        return dp.addGeo(geo2).build();
      case 21:
        return dp.addGeo(geo3).build();
      case 22:
        return dp.addGeo(geo3.build().toBuilder().clearCountry()).build();
      case 23:
        return dp.addGeo(geo3.build().toBuilder().clearRegion()).build();
      default:
        return _dp.build();
    }
  }

  @Test
  public void testAppProfile_invalid_dp() {
    for (int i = 0; i <= 23; i++) {
      try {
        tp.apply(Create.of(getSampleDp(i))).apply(new ComputeAppProfiles());
        tp.run();
        System.out.format("i = %d, failing.\n", i);
        fail();
      } catch (PipelineExecutionException e) {
        assertTrue(e.getCause() instanceof CorruptedDataException);
      }
    }
  }

  @Test
  public void testAppProfile_basic() {
    DeviceId.Builder did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1);
    DeviceId.Builder did2 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID2);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);
    AppActivity.Builder app5 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(8000L)
        .setLastAt(13000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.OPENX_VALUE, 3);

    // lifetime
    DeviceProfile.Builder life1 = DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app1).addApp(app2);
    DeviceProfile.Builder life2 = DeviceProfile.newBuilder().setDeviceId(did2).setFirstAt(8000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app2).addApp(app5);

    PAssert.that(tp.apply(Create.of(life1.build(), life2.build())).apply(new ComputeAppProfiles())).satisfies(out -> {
      assertEquals(2, Iterables.size(out));

      Map<String, AppProfile> profiles = new HashMap<>();
      for (AppProfile app : out) {
        profiles.put(app.getBundle(), app);
      }
      assertTrue(profiles.keySet().containsAll(Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle2)));

      {
        AppProfile _app = profiles.get(__TestBase.Bundle1);
        assertEquals(2, _app.getUserCount());
        assertEquals(2, _app.getUserCountPerExchangeOrThrow(Exchange.USF_VALUE));
        assertEquals(1, _app.getUserCountPerExchangeOrThrow(Exchange.OPENX_VALUE));
        assertEquals(1, _app.getUserCountPerExchangeOrThrow(Exchange.INMOBI_VALUE));
      }
      {
        AppProfile _app = profiles.get(__TestBase.Bundle2);
        assertEquals(2, _app.getUserCount());
        assertEquals(2, _app.getUserCountPerExchangeOrThrow(Exchange.USF_VALUE));
        assertEquals(2, _app.getUserCountPerExchangeOrThrow(Exchange.CS_VALUE));
      }
      return null;
    });
    tp.run();

  }

  @Test
  public void testAppProfile_all() {
    DeviceId.Builder did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1);
    DeviceId.Builder did2 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID2);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);
    AppActivity.Builder app5 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(8000L)
        .setLastAt(13000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.OPENX_VALUE, 3);

    // lifetime
    DeviceProfile.Builder life1 = DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app1).addApp(app2);
    DeviceProfile.Builder life2 = DeviceProfile.newBuilder().setDeviceId(did2).setFirstAt(8000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app2).addApp(app5);

    PAssert.that(tp.apply(Create.of(life1.build(), life2.build())).apply(new ComputeAppProfiles())).satisfies(out -> {
      assertEquals(2, Iterables.size(out));

      Map<String, AppProfile> profiles = new HashMap<>();
      for (AppProfile app : out) {
        profiles.put(app.getBundle(), app);
      }
      assertTrue(profiles.keySet().containsAll(Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle2)));

      {
        AppProfile _app = profiles.get(__TestBase.Bundle1);
        assertEquals(2, _app.getUserCount());
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.CS_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.ADX_VALUE, 0));
        assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.USF_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.UNITY_VALUE, 0));
        assertEquals(1, _app.getUserCountPerExchangeOrDefault(Exchange.OPENX_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.VUNGLE_VALUE, 0));
        assertEquals(1, _app.getUserCountPerExchangeOrDefault(Exchange.INMOBI_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.FYBER_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.APPLOVIN_VALUE, 0));
      }
      {
        AppProfile _app = profiles.get(__TestBase.Bundle2);
        assertEquals(2, _app.getUserCount());
        assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.CS_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.ADX_VALUE, 0));
        assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.USF_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.UNITY_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.OPENX_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.VUNGLE_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.INMOBI_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.FYBER_VALUE, 0));
        assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.APPLOVIN_VALUE, 0));
      }
      return null;
    });
    tp.run();

  }

  @Test
  public void testExceptionHandling04() {
    Pipeline p = tp;
    // duplicate DeviceIDs.
    try {
      p.apply(Create.of(
          DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1)).build(),
          DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1)).build()))
          .apply(new ComputeAppProfiles());
      tp.run();
      fail();
    } catch (PipelineExecutionException e) {
      assertTrue(e.getCause() instanceof CorruptedDataException);
    }
  }

  @Test
  public void testAppProfile_all2() {
    DeviceId.Builder did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1);
    DeviceId.Builder did2 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID2);
    DeviceId.Builder did3 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(__TestBase.UUID3);
    DeviceId.Builder did4 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(__TestBase.UUID4);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");
    GeoActivity.Builder geo2 = GeoActivity.newBuilder().setCountry("usa").setRegion("CA");
    GeoActivity.Builder geo3 = GeoActivity.newBuilder().setCountry("USA").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);
    AppActivity.Builder app3 = AppActivity.newBuilder().setBundle(__TestBase.Bundle3).setFirstAt(5000L)
        .setLastAt(10000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.ADX_VALUE, 3);
    AppActivity.Builder app4 = AppActivity.newBuilder().setBundle(__TestBase.Bundle4).setFirstAt(8000L).setLastAt(9000L)
        .putCountPerExchange(Exchange.APPLOVIN_VALUE, 2).putCountPerExchange(Exchange.UNITY_VALUE, 3);
    AppActivity.Builder app5 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(8000L)
        .setLastAt(13000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.OPENX_VALUE, 3);

    // lifetime
    DeviceProfile.Builder life1 = DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addGeo(geo2).addGeo(geo3).addApp(app1).addApp(app2);
    DeviceProfile.Builder life2 = DeviceProfile.newBuilder().setDeviceId(did2).setFirstAt(5000L).setLastAt(13000L)
        .addGeo(geo2).addGeo(geo3).addApp(app3).addApp(app5);
    DeviceProfile.Builder life3 = DeviceProfile.newBuilder().setDeviceId(did3).setFirstAt(5000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app2).addApp(app4).addApp(app3);
    DeviceProfile.Builder life4 = DeviceProfile.newBuilder().setDeviceId(did4).setFirstAt(5000L).setLastAt(10000L)
        .addGeo(geo1).addGeo(geo3).addApp(app4).addApp(app3);

    PAssert.that(
        tp.apply(Create.of(life1.build(), life2.build(), life3.build(), life4.build())).apply(new ComputeAppProfiles()))
        .satisfies(out -> {
          assertEquals(4, Iterables.size(out));

          Map<String, AppProfile> profiles = new HashMap<>();
          for (AppProfile app : out) {
            profiles.put(app.getBundle(), app);
          }
          assertTrue(profiles.keySet().containsAll(
              Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle2, __TestBase.Bundle3, __TestBase.Bundle4)));

          {
            AppProfile _app = profiles.get(__TestBase.Bundle1);
            assertEquals(2, _app.getUserCount());
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.CS_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.ADX_VALUE, 0));
            assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.USF_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.UNITY_VALUE, 0));
            assertEquals(1, _app.getUserCountPerExchangeOrDefault(Exchange.OPENX_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.VUNGLE_VALUE, 0));
            assertEquals(1, _app.getUserCountPerExchangeOrDefault(Exchange.INMOBI_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.FYBER_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.APPLOVIN_VALUE, 0));
          }
          {
            AppProfile _app = profiles.get(__TestBase.Bundle2);
            assertEquals(2, _app.getUserCount());
            assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.CS_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.ADX_VALUE, 0));
            assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.USF_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.UNITY_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.OPENX_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.VUNGLE_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.INMOBI_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.FYBER_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.APPLOVIN_VALUE, 0));
          }
          {
            AppProfile _app = profiles.get(__TestBase.Bundle3);
            assertEquals(3, _app.getUserCount());
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.CS_VALUE, 0));
            assertEquals(3, _app.getUserCountPerExchangeOrDefault(Exchange.ADX_VALUE, 0));
            assertEquals(3, _app.getUserCountPerExchangeOrDefault(Exchange.USF_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.UNITY_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.OPENX_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.VUNGLE_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.INMOBI_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.FYBER_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.APPLOVIN_VALUE, 0));
          }
          {
            AppProfile _app = profiles.get(__TestBase.Bundle4);
            assertEquals(2, _app.getUserCount());
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.CS_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.ADX_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.USF_VALUE, 0));
            assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.UNITY_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.OPENX_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.VUNGLE_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.INMOBI_VALUE, 0));
            assertEquals(0, _app.getUserCountPerExchangeOrDefault(Exchange.FYBER_VALUE, 0));
            assertEquals(2, _app.getUserCountPerExchangeOrDefault(Exchange.APPLOVIN_VALUE, 0));
          }
          return null;
        });
    tp.run();

  }

  @Test
  public void testAppProfile_allButMaps() {
    DeviceId.Builder did1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1);
    DeviceId.Builder did2 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.UUID2);
    DeviceId.Builder did3 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(__TestBase.UUID3);
    DeviceId.Builder did4 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(__TestBase.UUID4);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");
    GeoActivity.Builder geo2 = GeoActivity.newBuilder().setCountry("usa").setRegion("CA");
    GeoActivity.Builder geo3 = GeoActivity.newBuilder().setCountry("USA").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.CS_VALUE, 3);
    AppActivity.Builder app3 = AppActivity.newBuilder().setBundle(__TestBase.Bundle3).setFirstAt(5000L)
        .setLastAt(10000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.ADX_VALUE, 3);
    AppActivity.Builder app4 = AppActivity.newBuilder().setBundle(__TestBase.Bundle4).setFirstAt(8000L).setLastAt(9000L)
        .putCountPerExchange(Exchange.APPLOVIN_VALUE, 2).putCountPerExchange(Exchange.UNITY_VALUE, 3);
    AppActivity.Builder app5 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(8000L)
        .setLastAt(13000L).putCountPerExchange(Exchange.USF_VALUE, 2).putCountPerExchange(Exchange.OPENX_VALUE, 3);

    // lifetime
    DeviceProfile.Builder life1 = DeviceProfile.newBuilder().setDeviceId(did1).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addGeo(geo2).addGeo(geo3).addApp(app1).addApp(app2);
    DeviceProfile.Builder life2 = DeviceProfile.newBuilder().setDeviceId(did2).setFirstAt(5000L).setLastAt(13000L)
        .addGeo(geo2).addGeo(geo3).addApp(app3).addApp(app5);
    DeviceProfile.Builder life3 = DeviceProfile.newBuilder().setDeviceId(did3).setFirstAt(5000L).setLastAt(20000L)
        .addGeo(geo1).addApp(app2).addApp(app4).addApp(app3);
    DeviceProfile.Builder life4 = DeviceProfile.newBuilder().setDeviceId(did4).setFirstAt(5000L).setLastAt(10000L)
        .addGeo(geo1).addGeo(geo3).addApp(app4).addApp(app3);

    PAssert.that(
        tp.apply(Create.of(life1.build(), life2.build(), life3.build(), life4.build())).apply(new ComputeAppProfiles()))
        .satisfies(out -> {
          assertEquals(4, Iterables.size(out));

          Map<String, AppProfile> profiles = new HashMap<>();
          for (AppProfile app : out) {
            profiles.put(app.getBundle(), app);
          }
          assertTrue(profiles.keySet().containsAll(
              Arrays.asList(__TestBase.Bundle1, __TestBase.Bundle2, __TestBase.Bundle3, __TestBase.Bundle4)));

          {
            AppProfile _app = profiles.get(__TestBase.Bundle1);
            assertEquals(2, _app.getUserCount());
          }
          {
            AppProfile _app = profiles.get(__TestBase.Bundle2);
            assertEquals(2, _app.getUserCount());
          }
          {
            AppProfile _app = profiles.get(__TestBase.Bundle3);
            assertEquals(3, _app.getUserCount());
          }
          {
            AppProfile _app = profiles.get(__TestBase.Bundle4);
            assertEquals(2, _app.getUserCount());
          }
          return null;
        });
    tp.run();

  }
}
