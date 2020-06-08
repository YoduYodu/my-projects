package edu.usfca.dataflow.jobs2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Base64;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.Iterables;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.usfca.dataflow.transforms.Features.GetInputToModel;
import edu.usfca.dataflow.transforms.Predictions;
import edu.usfca.dataflow.utils.PathConfigs;
import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Data.PredictionData;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.InAppPurchaseProfile;

// TODO: Excluded from preview.

/**
 * These unit tests are essentially the same as the ones in __Test14.
 * <p>
 * The only difference is that, we're now testing performances (speed).
 */
public class __Test15PredictionJob {
  // This timeout is more than necessary for this specific test.
  @Rule
  public Timeout timeout = Timeout.millis(10000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  final static float EPS = 0.0001f;
  final static int[] II = new int[] {486, 686, 586, 777};

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  @Test
  public void testInputToModel03() {
    // We are creating "MULT" DeviceProfile messages per the original input (with new DeviceIDs),
    // compared to __Test14.
    // If this unit test times out, it's likely because you are using the starter code (for PredictDoFn) as-is,
    // which is extremely inefficient. Optimize it!
    final int MULT = 25;
    final PathConfigs config = PathConfigs.ofLocal();
    final DoFn<KV<DeviceId, float[]>, PredictionData> predictDoFn = Predictions.getPredictDoFn(config.getPathToModel());

    PCollection<String> dpBase64 = tp.apply(Create.of(
        "CigIAhIkMDEwNGYyZDAtOTI4My00YzI2LWJkODAtZDEyZGI4N2ZlMzNjKh0KCGFwcC4xMjM0EKRnGOuaBTIECAIQAjIECBUQAioiCgxBcHAuZnNvY2lldHkQgaYBGIPSBDIECAIQATIECBUQAUikZ1DrmgVaCQoDdXNhEgJDQVoJCgN1c2ESAmNh",
        "CigIAhIkMTgzNWVkNTAtMzA3Zi00YTQ1LThkODMtZmI0NDhhODViOTI5KiIKDEFwcC5mc29jaWV0eRDbsgIY/NwCMgQIAhABMgQIFRABKh4KCGFwcC4xMjM0EIPcAxiPnAUyBAgCEAIyBAgVEAFI27ICUI+cBVoJCgNVU0ESAm55WgkKA3VzYRICbnlaCQoDdXNhEgJOWQ==",
        "CigIAhIkMmQ0MDVlOTAtNTZjYy00ZDVhLThkY2MtODU3NzhkOGJmN2Q3KiIKDEFwcC5mc29jaWV0eRDV8gQY1YoFMgQIAhABMgQIFhABKh4KCEFwcC4xMjM0ENXEAxikuwQyBAgWEAIyBAgCEAJI1cQDUNWKBVoJCgN1c2ESAkNBWgkKA3VzYRICY2E=",
        "CigIAhIkMzEwNDIxOTAtMTFhNi00ZDRlLTk0ZGEtZThlMzdiMjdkMDNhKiEKDEFwcC5mc29jaWV0eRCYcRjYuQMyBAgJEAEyBAgVEAEqJAoIQXBwLjEyMzQQ7KsBGJrlBDIECAIQATIECAkQATIECBUQAUiYcVCa5QRaCQoDVVNBEgJueVoJCgN1c2ESAm55WgkKA3VzYRICTlk="))
        .apply(ParDo.of(new Explode(MULT)));

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

    final float[][] exp = new float[][] {{1.f / 6.f, 1.f / 3.f, 1.f / 8.f, 4f / 15f},
        {1.f / 6.f, 1.f / 3.f, 1.f / 8.f, 4f / 15f}, {0, 0, 0, 0}};

    PCollection<KV<DeviceId, float[]>> inputToModel =
        PCollectionList.of(dpBase64).and(suspiciousBase64).and(iappBase64).apply(new GetInputToModel());

    PAssert.that(inputToModel).satisfies(out -> {
      assertEquals(MULT * 4, Iterables.size(out));
      for (KV<DeviceId, float[]> kv : out) {
        DeviceId id = kv.getKey();
        int expIndex = -1;
        switch (id.getUuid().charAt(0)) {
          case '0':
            expIndex = 0;
            break;
          case '1':
            expIndex = 1;
            break;
          case '2':
          case '3':
            expIndex = 2;
            break;
          default:
            fail();
        }
        for (int j = 0; j < 4; j++) {
          assertEquals(exp[expIndex][j], kv.getValue()[II[j]], EPS);
        }
      }
      return null;
    });

    PCollection<PredictionData> predictions = inputToModel.apply(ParDo.of(predictDoFn));

    PAssert.that(predictions).satisfies(out -> {
      assertEquals(MULT * 4, Iterables.size(out));
      for (PredictionData pd : out) {
        switch (pd.getId().getUuid().charAt(0)) {
          case '0':
            assertEquals(15.202998161315918d, pd.getScore(), EPS);
            break;
          case '1':
            assertEquals(20.848115921020508d, pd.getScore(), EPS);
            break;
          case '2':
            assertEquals(13.728574752807617d, pd.getScore(), EPS);
            break;
          case '3':
            assertEquals(20.642118453979492d, pd.getScore(), EPS);
            break;
          default:
            fail();
        }
      }
      return null;
    });

    tp.run();
  }

  static class Explode extends DoFn<String, String> {
    final int multiplier;

    public Explode(int multiplier) {
      this.multiplier = multiplier;
    }

    @ProcessElement
    public void process(ProcessContext c) throws InvalidProtocolBufferException {
      DeviceProfile.Builder dp = DeviceProfile.parseFrom(Base64.getDecoder().decode(c.element())).toBuilder();
      DeviceId.Builder id = dp.getDeviceId().toBuilder();
      String uuidPrefix = id.getUuid().substring(0, 32);
      for (int i = 0; i < multiplier; i++) {
        final String newUuid = String.format("%s%04x", uuidPrefix, i);
        // System.out.println(newUuid);
        c.output(ProtoUtils.encodeMessageBase64(dp.setDeviceId(id.setUuid(newUuid)).build()));
      }
    }
  }

}
