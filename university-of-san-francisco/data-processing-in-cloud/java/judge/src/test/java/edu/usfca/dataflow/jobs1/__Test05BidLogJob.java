package edu.usfca.dataflow.jobs1;

import static edu.usfca.dataflow.__TestBase.Bundle1;
import static edu.usfca.dataflow.__TestBase.Bundle2;
import static edu.usfca.dataflow.__TestBase.Bundle3;
import static edu.usfca.dataflow.__TestBase.Bundle4;
import static edu.usfca.dataflow.__TestBase.UUID1;
import static edu.usfca.dataflow.__TestBase.UUID2;
import static edu.usfca.dataflow.__TestBase.UUID3;
import static edu.usfca.dataflow.__TestBase.UUID4;
import static edu.usfca.dataflow.transforms.SuspiciousIDs.getSuspiciousIDs;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import edu.usfca.dataflow.transforms.AppProfiles.ComputeAppProfiles;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;
import edu.usfca.protobuf.Profile.DeviceProfile.GeoActivity;

public class __Test05BidLogJob {
  @Rule
  public Timeout timeout = Timeout.millis(5000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  @Test
  public void testSuspiciousUsers1() {
    // User with UUID2 has too many unpopular apps.
    // User with UUID4 has too many Geos.
    PCollection<DeviceProfile> dps = tp.apply(Create.of( //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID2))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID3))
            .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID4))
            .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("WA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CO").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("NV").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("NY").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("IL").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("OR").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("MA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("PA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("MN").build()).build()//
    ));

    PCollection<AppProfile> aps = dps.apply(new ComputeAppProfiles());

    PAssert
        .that(getSuspiciousIDs(dps, aps, 4, 3, 8, 10).apply(MapElements.into(TypeDescriptor.of(DeviceId.class))
            .via((DeviceId id) -> id.toBuilder().setUuid(id.getUuid().toLowerCase()).build())))
        .containsInAnyOrder(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID2.toLowerCase()).build(), //
            DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID4.toLowerCase()).build());

    tp.run();
  }

  @Test
  public void testSuspiciousUsers2() {
    // User with Android/UUID2 is no longer suspicious because Bundle1 is now popular.
    PCollection<DeviceProfile> dps = tp.apply(Create.of( //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID2))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID3))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID4))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID2))
            .addApp(AppActivity.newBuilder().setBundle(Bundle1).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .addApp(AppActivity.newBuilder().setBundle(Bundle2).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID3))
            .addApp(AppActivity.newBuilder().setBundle(Bundle3).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build()).build(), //
        DeviceProfile.newBuilder().setDeviceId(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID4))
            .addApp(AppActivity.newBuilder().setBundle(Bundle4).setFirstAt(1234L).setLastAt(1234L)
                .putCountPerExchange(Exchange.CS_VALUE, 1).build())
            .setFirstAt(1234L).setLastAt(1234L)
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("WA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("CO").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("NV").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("NY").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("IL").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("OR").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("MA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("PA").build())
            .addGeo(GeoActivity.newBuilder().setCountry("US").setRegion("MN").build()).build()//
    ));

    PCollection<AppProfile> aps = dps.apply(new ComputeAppProfiles());

    PAssert
        .that(getSuspiciousIDs(dps, aps, 4, 3, 8, 10).apply(MapElements.into(TypeDescriptor.of(DeviceId.class))
            .via((DeviceId id) -> id.toBuilder().setUuid(id.getUuid().toLowerCase()).build())))
        .containsInAnyOrder(DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID4.toLowerCase()).build());

    tp.run();
  }
}
