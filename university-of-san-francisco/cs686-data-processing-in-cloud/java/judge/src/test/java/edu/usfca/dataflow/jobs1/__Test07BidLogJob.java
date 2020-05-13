package edu.usfca.dataflow.jobs1;

import static edu.usfca.dataflow.__TestHelper.areDeviceProfileSetsEqual;
import static edu.usfca.dataflow.__TestHelper.getDpWithCanonicalId;
import static edu.usfca.dataflow.transforms.SuspiciousIDs.getSuspiciousIDs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.usfca.dataflow.jobs1.BidLogJob.BidLog2DeviceProfile;
import edu.usfca.dataflow.transforms.AppProfiles.ComputeAppProfiles;
import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;

/**
 * This class tests your pipeline's correctness by using 22 BidLogs (which are all valid).
 *
 * In this small dataset, there are no suspicious IDs.
 */
public class __Test07BidLogJob {
  @Rule
  public Timeout timeout = Timeout.millis(5000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  // NOTE: It may be a good idea to decode these Base64-encoded proto messages (BidLogs),
  // and print them to the console so you can see the contents.
  static String[] bidLogsB64 = new String[] {
      "CoABCg5pZDA5MDgwLTU4ODI0MSIKQghhcHAuMTIzNCo3IgkaA3VzYSICQ0FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY2IIYmExNjY2MzSCAQhiYTI2MjM1OYoBCGJhNjUzOTc5igEIYmE1MzgwODUQFRig8wIgiPYHKAEw8So=",
      "CmoKDmlkMDI3OTItNTgzMzE2IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJjYXIDaW9zogEkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjYghiYTQ0MTU2MooBCGJhODA2MzM1EAIY65oFIKuSDygBMIZM",
      "CnUKDmlkMDI1ODItOTk0MDk5IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJDQXIDSU9TogEkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjYghiYTIyMDI5MoIBCGJhMzE5NTIwigEIYmExODAyMDUQFRij0QQggKsNKAEw+IEC",
      "CoABCg5pZDA2MDg3LTEwNjU1MyIKQghhcHAuMTIzNCo3IgkaA3VzYSICY2FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY2oIYmE2NDQyOTOCAQhiYTY5NDIwOIoBCGJhNzAzNTE1igEIYmE0MDIzNDEQAhikZyDK0wYoATDOgAE=",
      "CmsKDmlkMDE4MzItODAxMzY0IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJueXIDaW9zogEkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5aghiYTM1NDI4NIoBCWJhMTE5NTY0MBAVGI+cBSDkpQooATD4jQE=",
      "CmAKDmlkMDAzODQtMDMyMTA1IgpCCGFwcC4xMjM0KjciCRoDdXNhIgJOWXIDaU9zogEkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5igEIYmEyNTM4MDQQAhiD3AMglt0LKAEw5r4B",
      "CmsKDmlkMDc4NjMtODk5MTA0IgpCCGFwcC4xMjM0KjciCRoDVVNBIgJueXIDaW9zogEkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5agliYTExNTMwODeCAQhiYTkwMzUyNBACGOT9BCCdlQwoATCFZg==",
      "CngKDmlkMDY0OTktMDU2MzM5IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJDQXIDaW9zogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3ggEJYmExMjA1NDI1ggEIYmE5MzMyMjeKAQliYTEyMzgwNTUQFhjVxAMglasIKAEw44UB",
      "CnUKDmlkMDg0NTQtOTczMTE0IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJjYXIDaW9zogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3agliYTEyMTA0MzCCAQhiYTE5NTg4NIoBB2JhOTI1MzQQAhim0gMgtKEPKAEwqqEB",
      "Cl8KDmlkMDk4MTQtNTk0NzY1IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJDQXIDSU9TogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3YghiYTM1NDU3MBAWGKS7BCCBmw0oATC6Dw==",
      "Cn8KDmlkMDE2NjktNzc3NzMxIgpCCEFwcC4xMjM0KjciCRoDdXNhIgJjYXIDaW9zogEkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3aghiYTE3Mzc3NmoHYmE5NDk4NIIBCGJhNTE5NjQ3ggEJYmExMjQ2NDc1EAIYyJcEIILdCCgBMKLWAQ==",
      "CnQKDmlkMDE3MTAtMjY2MTQxIgpCCEFwcC4xMjM0KjciCRoDdXNhIgJueXIDaW9zogEkMzEwNDIxOWEtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhYghiYTI4NTU0MWIIYmE1Njc2NjGCAQhiYTM3Nzc5MhAVGOyrASCuiAooATCpkwE=",
      "CmsKDmlkMDgxNzMtNzcwNzI1IgpCCEFwcC4xMjM0KjciCRoDdXNhIgJOWXIDaU9zogEkMzEwNDIxOWEtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhagliYTEwNTEyMTCCAQhiYTk0NjA1OBAJGJqwAyCr5wwoATDOQA==",
      "CpQBCg5pZDA4MzIxLTU5MzE3NSIKQghBcHAuMTIzNCo3IgkaA1VTQSICbnlyA2lPU6IBJDMxMDQyMTlhLTExYTYtNGQ0ZS05NGRhLWU4ZTM3YjI3ZDAzYWIIYmE2MjI3ODRiCWJhMTEyMDgzMGoIYmE1ODYyNDNqCGJhMzQ5MTA4ggEIYmEzMzg3NzWKAQhiYTMxMTQxNhACGJrlBCDfog8oATC1nAE=",
      "CmEKDmlkMDY0MjgtNTIxMzI5IgtCCWFwcC5hMTIzNCo3IgkaA3VzYSICQ0FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY4IBCGJhNjM1MDc3EBUYgaYBIIqiCSgBMJ7jAQ==",
      "CmEKDmlkMDMxNjYtNjc5NTU2IgtCCWFwcC5iMTIzNCo3IgkaA3VzYSICY2FyA2lvc6IBJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzY4oBCGJhNTA1MjgzEAIYg9IEILn/BigBMNIc",
      "CpUBCg5pZDA5MTA4LTA1ODgwNSILQglhcHAuYzEyMzQqNyIJGgN1c2EiAm55cgNpb3OiASQxODM1ZWQ1My0zMDdmLTRhNDUtOGQ4My1mYjQ0OGE4NWI5MjliCWJhMTEyODI3NGIIYmExMDYzNDdqCGJhNzQxNDkwaghiYTg0NDY2NIIBCGJhODQzODgyigEIYmE1NTg5OTMQFRj83AIgrvsMKAEw0HI=",
      "CmwKDmlkMDM1NzEtNjIyOTgyIgtCCWFwcC5hMTIzNCo3IgkaA3VzYSICTllyA2lPc6IBJDE4MzVlZDUzLTMwN2YtNGE0NS04ZDgzLWZiNDQ4YTg1YjkyOWIIYmExMzE1NTCKAQliYTExMDAwNjYQAhjbsgIgmvIQKAEwsCk=",
      "CoEBCg5pZDAzMjA3LTk2NTE3OCILQglBcHAuYjEyMzQqNyIJGgN1c2EiAkNBcgNpb3OiASQyZDQwNWU5YS01NmNjLTRkNWEtOGRjYy04NTc3OGQ4YmY3ZDdiCWJhMTA0MDE2NWIJYmExMTYxMjE2aghiYTI4MjY0OYoBCGJhNDA4Mjg1EBYY1YoFILGdDigBMIKSAg==",
      "ClYKDmlkMDY5MTYtODU4NjExIgtCCUFwcC5jMTIzNCo3IgkaA3VzYSICY2FyA2lvc6IBJDJkNDA1ZTlhLTU2Y2MtNGQ1YS04ZGNjLTg1Nzc4ZDhiZjdkNxACGNXyBCCN1Q4oATD6cQ==",
      "CmAKDmlkMDQ3MDMtODUwNTc3IgtCCUFwcC5hMTIzNCo3IgkaA3VzYSICbnlyA2lvc6IBJDMxMDQyMTlhLTExYTYtNGQ0ZS05NGRhLWU4ZTM3YjI3ZDAzYWoIYmE0Mzc2ODUQFRiYcSCXmQ4oATDQlAE=",
      "CosBCg5pZDAyNTQ2LTcwMTQyMyILQglBcHAuYjEyMzQqNyIJGgN1c2EiAk5ZcgNpT3OiASQzMTA0MjE5YS0xMWE2LTRkNGUtOTRkYS1lOGUzN2IyN2QwM2FiCGJhOTc2NjExaghiYTI5ODY4MmoJYmExMjc2ODM3ggEIYmExNDAzODOKAQhiYTc2MzM1NhAJGNi5AyCktwkoATDAqQE="};

  Function<String, DeviceProfile> dpParser = x -> {
    try {
      return getDpWithCanonicalId(DeviceProfile.parseFrom(Base64.getDecoder().decode(x)));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  };

  Function<String, AppProfile> apParser = x -> {
    try {
      return AppProfile.parseFrom(Base64.getDecoder().decode(x));
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  };

  @Test
  public void testDeviceProfiles() {
    Set<DeviceProfile> expectedDeviceProfiles = Arrays.asList(
        "CigIAhIkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjKhkKCWFwcC5iMTIzNBCD0gQYg9IEMgQIAhABKh0KCGFwcC4xMjM0EKRnGOuaBTIECAIQAjIECBUQAioZCglhcHAuYTEyMzQQgaYBGIGmATIECBUQAUikZ1DrmgVaCQoDdXNhEgJDQVoJCgN1c2ESAmNh",
        "CigIAhIkMzEwNDIxOWEtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhKiQKCEFwcC4xMjM0EOyrARia5QQyBAgVEAEyBAgCEAEyBAgJEAEqFwoJQXBwLmExMjM0EJhxGJhxMgQIFRABKhkKCUFwcC5iMTIzNBDYuQMY2LkDMgQICRABSJhxUJrlBFoJCgNVU0ESAm55WgkKA3VzYRICTllaCQoDdXNhEgJueQ==",
        "CigIAhIkMTgzNWVkNTMtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5Kh4KCGFwcC4xMjM0EIPcAxiPnAUyBAgCEAIyBAgVEAEqGQoJYXBwLmMxMjM0EPzcAhj83AIyBAgVEAEqGQoJYXBwLmExMjM0ENuyAhjbsgIyBAgCEAFI27ICUI+cBVoJCgNVU0ESAm55WgkKA3VzYRICbnlaCQoDdXNhEgJOWQ==",
        "CigIAhIkMmQ0MDVlOWEtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3Kh4KCEFwcC4xMjM0ENXEAxikuwQyBAgWEAIyBAgCEAIqGQoJQXBwLmMxMjM0ENXyBBjV8gQyBAgCEAEqGQoJQXBwLmIxMjM0ENWKBRjVigUyBAgWEAFI1cQDUNWKBVoJCgN1c2ESAkNBWgkKA3VzYRICY2E=")
        .stream().map(dpParser).collect(Collectors.toSet());

    PCollection<byte[]> rawData = tp.apply(Create.of(Arrays.asList(bidLogsB64)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((String b) -> Base64.getDecoder().decode(b)));

    PCollection<DeviceProfile> deviceProfiles = rawData.apply(new BidLog2DeviceProfile());

    PAssert.that(deviceProfiles).satisfies(out -> {
      assertEquals(4, Iterables.size(out));
      Set<DeviceProfile> actualDeviceProfiles =
          StreamSupport.stream(out.spliterator(), false).map(x -> getDpWithCanonicalId(x)).collect(Collectors.toSet());
      try {
        assertTrue(areDeviceProfileSetsEqual(expectedDeviceProfiles, actualDeviceProfiles));
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }
      return null;
    });

    tp.run();
  }

  @Test
  public void testAppProfiles() {
    Set<AppProfile> expectedAppProfiles =
        Arrays.asList("CglBcHAuYjEyMzQQAhoECAkQARoECBYQAQ==", "CglhcHAuYzEyMzQQARoECBUQAQ==",
            "CghhcHAuMTIzNBACGgQIFRACGgQIAhAC", "CghBcHAuMTIzNBACGgQIFRABGgQIFhABGgQIAhACGgQICRAB",
            "CglhcHAuYTEyMzQQAhoECBUQARoECAIQAQ==", "CglhcHAuYjEyMzQQARoECAIQAQ==", "CglBcHAuYzEyMzQQARoECAIQAQ==",
            "CglBcHAuYTEyMzQQARoECBUQAQ==").stream().map(apParser).collect(Collectors.toSet());

    PCollection<byte[]> rawData = tp.apply(Create.of(Arrays.asList(bidLogsB64)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((String b) -> Base64.getDecoder().decode(b)));

    PCollection<AppProfile> appProfiles = rawData.apply(new BidLog2DeviceProfile()).apply(new ComputeAppProfiles());

    PAssert.that(appProfiles).satisfies(out -> {
      assertEquals(8, Iterables.size(out));
      assertEquals(expectedAppProfiles, StreamSupport.stream(out.spliterator(), false).collect(Collectors.toSet()));
      return null;
    });

    tp.run();
  }

  @Test
  public void testSuspiciousIDs() {

    PCollection<byte[]> rawData = tp.apply(Create.of(Arrays.asList(bidLogsB64)))
        .apply(MapElements.into(TypeDescriptor.of(byte[].class)).via((String b) -> Base64.getDecoder().decode(b)));

    PCollection<DeviceProfile> deviceProfiles = rawData.apply(new BidLog2DeviceProfile());
    PCollection<AppProfile> appProfiles = deviceProfiles.apply(new ComputeAppProfiles());

    PAssert.that(getSuspiciousIDs(deviceProfiles, appProfiles, 4, 3, 8, 10)).empty();

    tp.run();
  }
}
