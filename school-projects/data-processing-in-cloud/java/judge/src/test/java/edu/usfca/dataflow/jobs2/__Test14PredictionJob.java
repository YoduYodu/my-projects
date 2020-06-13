package edu.usfca.dataflow.jobs2;

import static edu.usfca.dataflow.__TestBase.UUID1;
import static edu.usfca.dataflow.__TestHelper.getCanonicalId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.usfca.dataflow.transforms.Features.GetInputToModel;
import edu.usfca.dataflow.transforms.Predictions;
import edu.usfca.dataflow.utils.PathConfigs;
import edu.usfca.dataflow.utils.PredictionUtils;
import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Data.PredictionData;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.InAppPurchaseProfile;

public class __Test14PredictionJob {
  @Rule
  public Timeout timeout = Timeout.millis(10000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  final static float EPS = 0.0001f;
  static final int I0 = 486;
  static final int I1 = 686;
  static final int I2 = 586;
  static final int I3 = 777;

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  static final PathConfigs config = PathConfigs.ofLocal();

  @Test
  public void dummy2() throws InvalidProtocolBufferException {
    String[] apps =
        new String[] {"app.33333ecad7", "app.6fd8dc2992", "app.4018edb0d4", "app.f888469a48", "app.6498651785",
            "app.a0ad812e7c", "app.5063d60bd7", "app.ef00612e1f", "app.c400fe5086", "app.eadf5060cd", "app.b9a053b190"};
    for (String app : apps) {
      InAppPurchaseProfile iap =
          InAppPurchaseProfile.newBuilder().setBundle(app).setNumPurchasers(1 + (app.hashCode() % 25 + 50) % 25)
              .setTotalAmount(17 + ((app.hashCode() % 250 + 500) % 250) * 10).build();
      System.out.println(ProtoUtils.encodeMessageBase64(iap));
      System.out.println(ProtoUtils.getJsonFromMessage(iap, true));
    }
  }

  // This is provided for your convenience.
  // You can check the contents of the proto messages that are used in the unit tests.
  @Ignore
  @Test
  public void dummy() throws InvalidProtocolBufferException {
    for (String str : new String[] {
        "CigIAhIkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjKh0KCGFwcC4xMjM0EKRnGOuaBTIECAIQAjIECBUQAioiCgxBcHAuZnNvY2lldHkQgaYBGIPSBDIECAIQATIECBUQAUikZ1DrmgVaCQoDdXNhEgJDQVoJCgN1c2ESAmNh",
        "CigIAhIkMTgzNWVkNTAtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5KiIKDEFwcC5mc29jaWV0eRDbsgIY/NwCMgQIAhABMgQIFRABKh4KCGFwcC4xMjM0EIPcAxiPnAUyBAgCEAIyBAgVEAFI27ICUI+cBVoJCgNVU0ESAm55WgkKA3VzYRICbnlaCQoDdXNhEgJOWQ==",
        "CigIAhIkMmQ0MDVlOTAtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3KiIKDEFwcC5mc29jaWV0eRDV8gQY1YoFMgQIAhABMgQIFhABKh4KCEFwcC4xMjM0ENXEAxikuwQyBAgWEAIyBAgCEAJI1cQDUNWKBVoJCgN1c2ESAkNBWgkKA3VzYRICY2E=",
        "CigIAhIkMzEwNDIxOTAtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhKiEKDEFwcC5mc29jaWV0eRCYcRjYuQMyBAgJEAEyBAgVEAEqJAoIQXBwLjEyMzQQ7KsBGJrlBDIECAIQATIECAkQATIECBUQAUiYcVCa5QRaCQoDVVNBEgJueVoJCgN1c2ESAm55WgkKA3VzYRICTlk=",
        "CigIAhIkNDM1MTBiNTAtOGQ3Ny00NWQ2LWJhNWMtNmJmY2ZhZmMyOWJlKh0KDUFwcC5mc29jaWV0eTIQ4eUCGOHlAjIECAkQASodCg1BcHAuZnNvY2lldHkxENyvBRjcrwUyBAgCEAEqHQoNQXBwLmZzb2NpZXR5NRDLxQMYy8UDMgQIAhABKh0KDUFwcC5mc29jaWV0eTQQzLgBGMy4ATIECAQQASobCg1BcHAuZnNvY2lldHkzELxvGLxvMgQIARABSLxvUNyvBVoJCgNVU0ESAk5ZWgkKA1VTQRICbnk=",
        "CigIAhIkNTc4MTk2YTAtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4Kl0KDEFwcC5hYnVzaW5nMRD5ZRjD/QMyBAgWEAEyBAgIEAEyBAgEEAEyBAgVEAEyBAgDEAEyBAgHEAEyBAgGEAEyBAgFEAEyBAgCEAEyBAgBEAEyBAgKEAEyBAgJEAEqXQoMQXBwLmFidXNpbmcyEKZ5GIDfBDIECBYQATIECBUQATIECAcQATIECAMQATIECAoQATIECAIQATIECAYQATIECAUQATIECAQQATIECAEQATIECAkQATIECAgQAUj5ZVCA3wRaCQoDVVNBEgJXQQ==",
        "CigIAhIkNjc5ZDgyYjAtOGFhYi00OTk1LTllYWUtZWM3ZjkyMjUxOWQ4KjoKDEFwcC5mc29jaWV0eRDjhgEY+ZwFMgQICRABMgQIAhAJMgQIFhABMgQIFRABMgQIBBABMgQIARABSOOGAVD5nAVaCQoDREVOEgJmcloJCgNVc2ESAm55WgwKA0pQThIFa3lvdG9aCwoDS09SEgRqZWp1WgkKA1VTQRICbnlaCQoDQ0FOEgJPTloKCgNCUkESA3NhbloKCgNNRVgSA2NhbloMCgNJVEESBW1pbGFuWg0KA0NIThIGcGVraW5nWggKA0ZSQRIBcFoNCgNHQlISBmxvbmRvbloJCgN1c2ESAm55WgkKA3VzQRICbnk="}) {
      DeviceProfile xx = ProtoUtils.decodeMessageBase64(DeviceProfile.parser(), str);
      System.out.println(ProtoUtils.getJsonFromMessage(xx, true));
    }
    System.out.println();
    for (String str : new String[] {"CAISJDQzNTEwYjUwLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZQ==",
        "CAISJDU3ODE5NmEwLTcyMmUtNDQxYS1iMDE3LWRlNjkyY2NkZjViOA==",
        "CAISJDY3OWQ4MmIwLThhYWItNDk5NS05ZWFlLWVjN2Y5MjI1MTlkOA=="}) {
      DeviceId xx = ProtoUtils.decodeMessageBase64(DeviceId.parser(), str);
      System.out.println(ProtoUtils.getJsonFromMessage(xx, true));
    }
  }

  @Test
  public void testInputToModel01() {
    PathConfigs config = PathConfigs.ofLocal();

    PCollection<String> dpBase64 = tp.apply(Create.of(
        "CigIAhIkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjKh0KCGFwcC4xMjM0EKRnGOuaBTIECAIQAjIECBUQAioiCgxBcHAuZnNvY2lldHkQgaYBGIPSBDIECAIQATIECBUQAUikZ1DrmgVaCQoDdXNhEgJDQVoJCgN1c2ESAmNh",
        "CigIAhIkMTgzNWVkNTAtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5KiIKDEFwcC5mc29jaWV0eRDbsgIY/NwCMgQIAhABMgQIFRABKh4KCGFwcC4xMjM0EIPcAxiPnAUyBAgCEAIyBAgVEAFI27ICUI+cBVoJCgNVU0ESAm55WgkKA3VzYRICbnlaCQoDdXNhEgJOWQ==",
        "CigIAhIkMmQ0MDVlOTAtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3KiIKDEFwcC5mc29jaWV0eRDV8gQY1YoFMgQIAhABMgQIFhABKh4KCEFwcC4xMjM0ENXEAxikuwQyBAgWEAIyBAgCEAJI1cQDUNWKBVoJCgN1c2ESAkNBWgkKA3VzYRICY2E=",
        "CigIAhIkMzEwNDIxOTAtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhKiEKDEFwcC5mc29jaWV0eRCYcRjYuQMyBAgJEAEyBAgVEAEqJAoIQXBwLjEyMzQQ7KsBGJrlBDIECAIQATIECAkQATIECBUQAUiYcVCa5QRaCQoDVVNBEgJueVoJCgN1c2ESAm55WgkKA3VzYRICTlk=",
        "CigIAhIkNDM1MTBiNTAtOGQ3Ny00NWQ2LWJhNWMtNmJmY2ZhZmMyOWJlKh0KDUFwcC5mc29jaWV0eTIQ4eUCGOHlAjIECAkQASodCg1BcHAuZnNvY2lldHkxENyvBRjcrwUyBAgCEAEqHQoNQXBwLmZzb2NpZXR5NRDLxQMYy8UDMgQIAhABKh0KDUFwcC5mc29jaWV0eTQQzLgBGMy4ATIECAQQASobCg1BcHAuZnNvY2lldHkzELxvGLxvMgQIARABSLxvUNyvBVoJCgNVU0ESAk5ZWgkKA1VTQRICbnk=",
        "CigIAhIkNTc4MTk2YTAtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4Kl0KDEFwcC5hYnVzaW5nMRD5ZRjD/QMyBAgWEAEyBAgIEAEyBAgEEAEyBAgVEAEyBAgDEAEyBAgHEAEyBAgGEAEyBAgFEAEyBAgCEAEyBAgBEAEyBAgKEAEyBAgJEAEqXQoMQXBwLmFidXNpbmcyEKZ5GIDfBDIECBYQATIECBUQATIECAcQATIECAMQATIECAoQATIECAIQATIECAYQATIECAUQATIECAQQATIECAEQATIECAkQATIECAgQAUj5ZVCA3wRaCQoDVVNBEgJXQQ==",
        "CigIAhIkNjc5ZDgyYjAtOGFhYi00OTk1LTllYWUtZWM3ZjkyMjUxOWQ4KjoKDEFwcC5mc29jaWV0eRDjhgEY+ZwFMgQICRABMgQIAhAJMgQIFhABMgQIFRABMgQIBBABMgQIARABSOOGAVD5nAVaCQoDREVOEgJmcloJCgNVc2ESAm55WgwKA0pQThIFa3lvdG9aCwoDS09SEgRqZWp1WgkKA1VTQRICbnlaCQoDQ0FOEgJPTloKCgNCUkESA3NhbloKCgNNRVgSA2NhbloMCgNJVEESBW1pbGFuWg0KA0NIThIGcGVraW5nWggKA0ZSQRIBcFoNCgNHQlISBmxvbmRvbloJCgN1c2ESAm55WgkKA3VzQRICbnk="));

    PCollection<String> suspiciousBase64 =
        tp.apply(Create.of("CAISJDQzNTEwYjUwLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZQ==",
            "CAISJDU3ODE5NmEwLTcyMmUtNDQxYS1iMDE3LWRlNjkyY2NkZjViOA==",
            "CAISJDY3OWQ4MmIwLThhYWItNDk5NS05ZWFlLWVjN2Y5MjI1MTlkOA=="));

    // Testing the degenerate case where iappBase64 is empty.

    PCollection<String> iappBase64 = tp.apply(Create.empty(StringUtf8Coder.of()));

    DeviceId id1 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID1).build();

    Map<DeviceId, float[]> expected = Arrays.asList("CAISJDAxMDRmMmQwLTkyODMtNGMyNi1iZDgwLWQxMmRiODdmZTMzYw==",
        "CAISJDE4MzVlZDUwLTMwN2YtNGE0NS04ZDgzLWZiNDQ4YTg1YjkyOQ==",
        "CAISJDMxMDQyMTkwLTExYTYtNGQ0ZS05NGRhLWU4ZTM3YjI3ZDAzYQ==",
        "CAISJDJkNDA1ZTkwLTU2Y2MtNGQ1YS04ZGNjLTg1Nzc4ZDhiZjdkNw==").stream().map(x -> {
          try {
            return ProtoUtils.decodeMessageBase64(DeviceId.parser(), x);
          } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
          }
          return null;
        }).collect(Collectors.toMap(x -> x, x -> PredictionUtils.getBaseData(x)));

    PCollection<KV<DeviceId, float[]>> inputToModel =
        PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel());

    PAssert.that(inputToModel).satisfies(out -> {

      assertEquals(4, Iterables.size(out));
      for (KV<DeviceId, float[]> kv : out) {
        DeviceId id = getCanonicalId(kv.getKey());
        System.out.println(id.toString());
        assertTrue(expected.containsKey(id));
        float[] exp = expected.get(id);
        assertEquals(784, kv.getValue().length);
        for (int i = 0; i < 784; i++) {
          if (i == I0 || i == I1 || i == I2 || i == I3 || (i >= 100 && i <= 700 && i % 40 == 20)) {
            continue;
          }
          assertEquals(exp[i], kv.getValue()[i], EPS);
        }
      }
      return null;
    });

    PCollection<PredictionData> predictions =
        inputToModel.apply(ParDo.of(Predictions.getPredictDoFn(config.getPathToModel())));

    PAssert.that(predictions).satisfies(out -> {
      for (PredictionData pd : out) {
        try {
          System.out.println(ProtoUtils.getJsonFromMessage(pd, true));
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        switch (pd.getId().getUuid().toLowerCase()) {
          case "0104f2d0-9283-4c26-bd80-d12db87fe33c":
            assertEquals(1, pd.getPrediction());
            assertEquals(15.381132125854492d, pd.getScore(), EPS);
            break;

          case "1835ed50-307f-4a45-8d83-fb448a85b929":
            assertEquals(0, pd.getPrediction());
            assertEquals(20.913591384887695, pd.getScore(), EPS);
            break;

          case "2d405e90-56cc-4d5a-8dcc-85778d8bf7d7":
            assertEquals(0, pd.getPrediction());
            assertEquals(13.728574752807617d, pd.getScore(), EPS);
            break;

          case "31042190-11a6-4d4e-94da-e8e37b27d03a":
            assertEquals(2, pd.getPrediction());
            assertEquals(20.642118453979492d, pd.getScore(), EPS);
            break;

          default:
            // should not be reached.
            fail();
        }
      }
      return null;
    });

    tp.run();
  }

  @Test
  public void testInputToModel02() {
    PCollection<String> dpBase64 = tp.apply(Create.of(
        "CigIAhIkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjKh0KCGFwcC4xMjM0EKRnGOuaBTIECAIQAjIECBUQAioiCgxBcHAuZnNvY2lldHkQgaYBGIPSBDIECAIQATIECBUQAUikZ1DrmgVaCQoDdXNhEgJDQVoJCgN1c2ESAmNh",
        "CigIAhIkMTgzNWVkNTAtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5KiIKDEFwcC5mc29jaWV0eRDbsgIY/NwCMgQIAhABMgQIFRABKh4KCGFwcC4xMjM0EIPcAxiPnAUyBAgCEAIyBAgVEAFI27ICUI+cBVoJCgNVU0ESAm55WgkKA3VzYRICbnlaCQoDdXNhEgJOWQ==",
        "CigIAhIkMmQ0MDVlOTAtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3KiIKDEFwcC5mc29jaWV0eRDV8gQY1YoFMgQIAhABMgQIFhABKh4KCEFwcC4xMjM0ENXEAxikuwQyBAgWEAIyBAgCEAJI1cQDUNWKBVoJCgN1c2ESAkNBWgkKA3VzYRICY2E=",
        "CigIAhIkMzEwNDIxOTAtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhKiEKDEFwcC5mc29jaWV0eRCYcRjYuQMyBAgJEAEyBAgVEAEqJAoIQXBwLjEyMzQQ7KsBGJrlBDIECAIQATIECAkQATIECBUQAUiYcVCa5QRaCQoDVVNBEgJueVoJCgN1c2ESAm55WgkKA3VzYRICTlk=",
        "CigIAhIkNDM1MTBiNTAtOGQ3Ny00NWQ2LWJhNWMtNmJmY2ZhZmMyOWJlKh0KDUFwcC5mc29jaWV0eTIQ4eUCGOHlAjIECAkQASodCg1BcHAuZnNvY2lldHkxENyvBRjcrwUyBAgCEAEqHQoNQXBwLmZzb2NpZXR5NRDLxQMYy8UDMgQIAhABKh0KDUFwcC5mc29jaWV0eTQQzLgBGMy4ATIECAQQASobCg1BcHAuZnNvY2lldHkzELxvGLxvMgQIARABSLxvUNyvBVoJCgNVU0ESAk5ZWgkKA1VTQRICbnk=",
        "CigIAhIkNTc4MTk2YTAtNzIyZS00NDFhLWIwMTctZGU2OTJjY2RmNWI4Kl0KDEFwcC5hYnVzaW5nMRD5ZRjD/QMyBAgWEAEyBAgIEAEyBAgEEAEyBAgVEAEyBAgDEAEyBAgHEAEyBAgGEAEyBAgFEAEyBAgCEAEyBAgBEAEyBAgKEAEyBAgJEAEqXQoMQXBwLmFidXNpbmcyEKZ5GIDfBDIECBYQATIECBUQATIECAcQATIECAMQATIECAoQATIECAIQATIECAYQATIECAUQATIECAQQATIECAEQATIECAkQATIECAgQAUj5ZVCA3wRaCQoDVVNBEgJXQQ==",
        "CigIAhIkNjc5ZDgyYjAtOGFhYi00OTk1LTllYWUtZWM3ZjkyMjUxOWQ4KjoKDEFwcC5mc29jaWV0eRDjhgEY+ZwFMgQICRABMgQIAhAJMgQIFhABMgQIFRABMgQIBBABMgQIARABSOOGAVD5nAVaCQoDREVOEgJmcloJCgNVc2ESAm55WgwKA0pQThIFa3lvdG9aCwoDS09SEgRqZWp1WgkKA1VTQRICbnlaCQoDQ0FOEgJPTloKCgNCUkESA3NhbloKCgNNRVgSA2NhbloMCgNJVEESBW1pbGFuWg0KA0NIThIGcGVraW5nWggKA0ZSQRIBcFoNCgNHQlISBmxvbmRvbloJCgN1c2ESAm55WgkKA3VzQRICbnk="));

    PCollection<String> suspiciousBase64 =
        tp.apply(Create.of("CAISJDQzNTEwYjUwLThkNzctNDVkNi1iYTVjLTZiZmNmYWZjMjliZQ==",
            "CAISJDU3ODE5NmEwLTcyMmUtNDQxYS1iMDE3LWRlNjkyY2NkZjViOA==",
            "CAISJDY3OWQ4MmIwLThhYWItNDk5NS05ZWFlLWVjN2Y5MjI1MTlkOA=="));

    // IAPP data contains five apps.
    PCollection<String> iappBase64 = tp.apply(Create.of(//
        InAppPurchaseProfile.newBuilder().setBundle("App.abusing0").setNumPurchasers(5L).setTotalAmount(1000L).build(), //
        InAppPurchaseProfile.newBuilder().setBundle("App.fsociety1").setNumPurchasers(4L).setTotalAmount(2000L).build(), //
        InAppPurchaseProfile.newBuilder().setBundle("App.fsociety9").setNumPurchasers(3L).setTotalAmount(3000L).build(), //
        InAppPurchaseProfile.newBuilder().setBundle("app.1234").setNumPurchasers(2L).setTotalAmount(4000L).build(), //
        InAppPurchaseProfile.newBuilder().setBundle("App.fsociety6").setNumPurchasers(1L).setTotalAmount(5000L).build()//
    )).apply(
        MapElements.into(TypeDescriptors.strings()).via((InAppPurchaseProfile x) -> ProtoUtils.encodeMessageBase64(x)));

    // The first two users below have "app.1234" and "App.fsociety" in their AppActivity.
    // The other two have no apps in the IAPP data above.
    Map<DeviceId, float[]> expected = new ImmutableMap.Builder<DeviceId, float[]>()
        .put(DeviceId.newBuilder().setOs(OsType.IOS).setUuid("0104f2d0-9283-4c26-bd80-d12db87fe33c").build(),
            new float[] {1.f / 6.f, 1.f / 3.f, 1.f / 8.f, 4f / 15f})
        .put(DeviceId.newBuilder().setOs(OsType.IOS).setUuid("1835ed50-307f-4a45-8d83-fb448a85b929").build(),
            new float[] {1.f / 6.f, 1.f / 3.f, 1.f / 8.f, 4f / 15f})
        .put(DeviceId.newBuilder().setOs(OsType.IOS).setUuid("2d405e90-56cc-4d5a-8dcc-85778d8bf7d7").build(),
            new float[] {0, 0, 0, 0})
        .put(DeviceId.newBuilder().setOs(OsType.IOS).setUuid("31042190-11a6-4d4e-94da-e8e37b27d03a").build(),
            new float[] {0, 0, 0, 0})
        .build();

    PCollection<KV<DeviceId, float[]>> inputToModel =
        PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel());

    // After removing suspicious IDs, we're left with 4 users with input to model being 0's.
    PAssert.that(inputToModel).satisfies(out -> {
      assertEquals(4, Iterables.size(out));
      for (KV<DeviceId, float[]> kv : out) {
        // This will be useful for debugging.
        System.out.format("%s %s %f %f %f %f\n", kv.getKey().getOs(), kv.getKey().getUuid(), kv.getValue()[I0],
            kv.getValue()[I1], kv.getValue()[I2], kv.getValue()[I3]);

        DeviceId id = getCanonicalId(kv.getKey());
        assertTrue(expected.containsKey(id));
        assertEquals(expected.get(id)[0], kv.getValue()[I0], EPS);
        assertEquals(expected.get(id)[1], kv.getValue()[I1], EPS);
        assertEquals(expected.get(id)[2], kv.getValue()[I2], EPS);
        assertEquals(expected.get(id)[3], kv.getValue()[I3], EPS);
      }
      return null;
    });

    PCollection<PredictionData> predictions =
        inputToModel.apply(ParDo.of(Predictions.getPredictDoFn(config.getPathToModel())));

    PAssert.that(predictions).satisfies(out -> {
      for (PredictionData pd : out) {
        try {
          System.out.println(ProtoUtils.getJsonFromMessage(pd, true));
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        switch (pd.getId().getUuid().toLowerCase()) {
          case "0104f2d0-9283-4c26-bd80-d12db87fe33c":
            assertEquals(1, pd.getPrediction());
            assertEquals(15.202998161315918d, pd.getScore(), EPS);
            break;

          case "1835ed50-307f-4a45-8d83-fb448a85b929":
            assertEquals(0, pd.getPrediction());
            assertEquals(20.848115921020508d, pd.getScore(), EPS);
            break;

          case "2d405e90-56cc-4d5a-8dcc-85778d8bf7d7":
            assertEquals(0, pd.getPrediction());
            assertEquals(13.728574752807617d, pd.getScore(), EPS);
            break;

          case "31042190-11a6-4d4e-94da-e8e37b27d03a":
            assertEquals(2, pd.getPrediction());
            assertEquals(20.642118453979492d, pd.getScore(), EPS);
            break;

          default:
            // should not be reached.
            fail();
        }
      }
      return null;
    });

    tp.run();
  }
}
