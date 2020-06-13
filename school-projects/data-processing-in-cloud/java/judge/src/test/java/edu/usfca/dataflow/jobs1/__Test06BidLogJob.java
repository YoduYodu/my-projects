package edu.usfca.dataflow.jobs1;

import static edu.usfca.dataflow.__TestBase.Bundle1;
import static edu.usfca.dataflow.__TestBase.Bundle2;
import static edu.usfca.dataflow.__TestBase.Bundle3;
import static edu.usfca.dataflow.__TestBase.Bundle4;
import static edu.usfca.dataflow.__TestBase.Bundle5;
import static edu.usfca.dataflow.__TestBase.UUID0;
import static edu.usfca.dataflow.__TestBase.UUID1;
import static edu.usfca.dataflow.__TestBase.UUID2;
import static edu.usfca.dataflow.__TestBase.UUID3;
import static edu.usfca.dataflow.__TestBase.UUID4;
import static edu.usfca.dataflow.__TestBase.UUID5;
import static edu.usfca.dataflow.__TestBase.UUID6;
import static edu.usfca.dataflow.__TestBase.UUID7;
import static edu.usfca.dataflow.__TestBase.UUID8;
import static edu.usfca.dataflow.__TestBase.UUID9;
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

import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.App;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Geo;

import edu.usfca.dataflow.jobs1.BidLogJob.BidLog2DeviceProfile;
import edu.usfca.dataflow.transforms.AppProfiles.ComputeAppProfiles;
import edu.usfca.protobuf.Bid.BidLog;
import edu.usfca.protobuf.Bid.BidResult;
import edu.usfca.protobuf.Bid.Exchange;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;

public class __Test06BidLogJob {
  @Rule
  public Timeout timeout = Timeout.millis(5000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  final static DeviceId id1 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1).build();
  final static DeviceId id2 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID2).build();
  final static DeviceId id3 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID3).build();
  final static DeviceId id4 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID4).build();
  final static DeviceId id5 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID5).build();
  final static DeviceId id6 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID6).build();
  final static DeviceId id7 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID7).build();
  final static DeviceId id8 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID8).build();
  final static DeviceId id9 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID9).build();
  final static DeviceId id0 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID0).build();
  final static DeviceId id11 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1).build();
  final static DeviceId id12 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID2).build();
  final static DeviceId id13 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID3).build();
  final static DeviceId id14 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID4).build();
  final static DeviceId id15 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID5).build();
  final static DeviceId id16 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID6).build();
  final static DeviceId id17 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID7).build();
  final static DeviceId id18 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID8).build();
  final static DeviceId id19 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID9).build();
  final static DeviceId id10 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID0).build();

  static BidLog getBidLogSample(DeviceId id, Exchange exchange, String bundle) {
    return BidLog.newBuilder()
        .setBidRequest(BidRequest.newBuilder().setId("test").setApp(App.newBuilder().setBundle(bundle))
            .setDevice(Device.newBuilder().setOs(id.getOs().name()).setIfa(id.getUuid())
                .setGeo(Geo.newBuilder().setCountry("us").setRegion("ca").build()))
            .build())
        .setReceivedAt(12345678L).setExchange(exchange).setProcessedAt(123456789L).setBidResult(BidResult.BID)
        .setBidPrice(1234).build();
  }

  @Test
  public void testSuspiciousUsers1() {
    PCollection<byte[]> bidLogRaw = tp.apply(Create.of(getBidLogSample(id0, Exchange.forNumber(5), Bundle1), //
        getBidLogSample(id0, Exchange.forNumber(1), Bundle1), //
        getBidLogSample(id1, Exchange.forNumber(2), Bundle2), //
        getBidLogSample(id2, Exchange.forNumber(3), Bundle3), //
        getBidLogSample(id3, Exchange.forNumber(4), Bundle4), //
        getBidLogSample(id4, Exchange.forNumber(5), Bundle5), //
        getBidLogSample(id5, Exchange.forNumber(6), Bundle4), //
        getBidLogSample(id6, Exchange.forNumber(7), Bundle3), //
        getBidLogSample(id7, Exchange.forNumber(8), Bundle2), //
        getBidLogSample(id8, Exchange.forNumber(9), Bundle1), //
        getBidLogSample(id9, Exchange.forNumber(10), Bundle2), //
        getBidLogSample(id10, Exchange.forNumber(21), Bundle3), //
        getBidLogSample(id11, Exchange.forNumber(22), Bundle4), //
        getBidLogSample(id12, Exchange.forNumber(21), Bundle5), //
        getBidLogSample(id13, Exchange.forNumber(10), Bundle4), //
        getBidLogSample(id14, Exchange.forNumber(9), Bundle3), //
        getBidLogSample(id15, Exchange.forNumber(8), Bundle2), //
        getBidLogSample(id16, Exchange.forNumber(7), Bundle1), //
        getBidLogSample(id17, Exchange.forNumber(6), Bundle2), //
        getBidLogSample(id18, Exchange.forNumber(5), Bundle3), //
        getBidLogSample(id19, Exchange.forNumber(4), Bundle4)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((BidLog bl) -> bl.toByteArray()));

    PCollection<DeviceProfile> dps = bidLogRaw.apply(new BidLog2DeviceProfile());

    PCollection<AppProfile> aps = dps.apply(new ComputeAppProfiles());

    PAssert.that(getSuspiciousIDs(dps, aps, 4, 3, 8, 10)).empty();

    tp.run();
  }
}
