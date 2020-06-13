package edu.usfca.dataflow.utils;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import edu.usfca.dataflow.__TestBase;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;

/**
 * Instructions:
 *
 * These unit tests should pass, after you fix silly bugs in DeviceProfileUtils class.
 *
 * If you choose to write your own methods, make sure you test them.
 */
public class __Test02DeviceProfileUtils {
  // Note: This rule is used to stop the grading system from going down when your code gets stuck (safety measure).
  // If you think this prevents your submission from getting graded normally, ask on Piazza.
  @Rule
  public Timeout timeout = Timeout.millis(2000);

  @Test
  public void testMergeDegenerateCase() {
    assertEquals(DeviceProfile.getDefaultInstance(), DeviceProfileUtils.mergeDps(new ArrayList<DeviceProfile>()));
  }

  @Test
  public void testIsDeviceIdValid() {
    assertTrue(DeviceProfileUtils
        .isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.Bundle1).build()));
    assertTrue(DeviceProfileUtils
        .isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.Bundle1.toUpperCase()).build()));
    assertTrue(DeviceProfileUtils.isDeviceIdValid(
        DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(__TestBase.Bundle1.toUpperCase()).build()));

    assertFalse(DeviceProfileUtils.isDeviceIdValid(
        DeviceId.newBuilder().setOs(OsType.UNKNOWN_OS_TYPE).setUuid(__TestBase.Bundle1.toUpperCase()).build()));
    assertFalse(DeviceProfileUtils
        .isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.UNKNOWN_OS_TYPE).setUuid(__TestBase.Bundle1).build()));
    assertFalse(DeviceProfileUtils.isDeviceIdValid(DeviceId.newBuilder().setUuid(__TestBase.Bundle1).build()));

    assertFalse(DeviceProfileUtils.isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(" ").build()));
    assertFalse(DeviceProfileUtils.isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid("\n").build()));
    assertFalse(DeviceProfileUtils.isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid("").build()));
    assertFalse(DeviceProfileUtils.isDeviceIdValid(DeviceId.newBuilder().setOs(OsType.ANDROID).build()));
  }

  @Test
  public void testIsDeviceIdValid2() {
    DeviceId.Builder did = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(__TestBase.Bundle1);

    GeoActivity.Builder geo1 = GeoActivity.newBuilder().setCountry("usa").setRegion("ca");
    GeoActivity.Builder geo2 = GeoActivity.newBuilder().setCountry("usa").setRegion("CA");
    GeoActivity.Builder geo3 = GeoActivity.newBuilder().setCountry("USA").setRegion("ca");

    AppActivity.Builder app1 = AppActivity.newBuilder().setBundle(__TestBase.Bundle1).setFirstAt(10000L)
        .setLastAt(12000L).putCountPerExchange(Exchange.INMOBI_VALUE, 2).putCountPerExchange(Exchange.MOPUB_VALUE, 3);
    AppActivity.Builder app2 = AppActivity.newBuilder().setBundle(__TestBase.Bundle2).setFirstAt(15000L)
        .setLastAt(20000L).putCountPerExchange(Exchange.MOPUB_VALUE, 2).putCountPerExchange(Exchange.USF_VALUE, 3);

    DeviceProfile.Builder _dp = DeviceProfile.newBuilder().setDeviceId(did).setFirstAt(10000L).setLastAt(20000L)
        .addGeo(geo1).addGeo(geo2).addGeo(geo3).addApp(app1).addApp(app2), dp;

    dp = _dp.build().toBuilder();
    assertTrue(DeviceProfileUtils.isDpValid(dp.build()));

    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.clearDeviceId().build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.setDeviceId(did.build().toBuilder().clearOs()).build()));
    dp = _dp.build().toBuilder();
    assertFalse(
        DeviceProfileUtils.isDpValid(dp.setDeviceId(did.build().toBuilder().setOs(OsType.UNKNOWN_OS_TYPE)).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.setDeviceId(did.build().toBuilder().clearUuid()).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.setDeviceId(did.build().toBuilder().setUuid(" ")).build()));

    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.clearApp().build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.removeApp(0).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.removeApp(1).build()));

    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addApp(dp.getApp(0)).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addApp(dp.getApp(1)).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(
        dp.addApp(AppActivity.newBuilder().setBundle("").setFirstAt(12345L).setLastAt(12345L).build()).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils
        .isDpValid(dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).clearCountPerExchange()).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(
        dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).putCountPerExchange(15, 1)).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(
        dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).putCountPerExchange(5, 0)).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp
        .addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).setFirstAt(20001L).setLastAt(20001L)).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(
        dp.addApp(app2.build().toBuilder().setBundle(__TestBase.Bundle3).setFirstAt(2001L).setLastAt(2001L)).build()));

    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.clearApp().clearFirstAt().build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.clearApp().clearLastAt().build()));

    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addGeo(geo1).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addGeo(geo2).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addGeo(geo3).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addGeo(geo3.build().toBuilder().clearCountry()).build()));
    dp = _dp.build().toBuilder();
    assertFalse(DeviceProfileUtils.isDpValid(dp.addGeo(geo3.build().toBuilder().clearRegion()).build()));
  }
}
