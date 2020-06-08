package edu.usfca.dataflow.jobs2;

import static edu.usfca.dataflow.__TestBase.UUID1;
import static edu.usfca.dataflow.__TestBase.UUID2;
import static edu.usfca.dataflow.__TestBase.UUID3;
import static edu.usfca.dataflow.__TestBase.UUID4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.usfca.dataflow.transforms.Predictions;
import edu.usfca.dataflow.utils.DeviceProfileUtils;
import edu.usfca.dataflow.utils.PathConfigs;
import edu.usfca.dataflow.utils.PredictionUtils;
import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Common.OsType;
import edu.usfca.protobuf.Data.PredictionData;

/**
 * Note: This unit tests should pass without any changes to the starter code.
 *
 * If not, there may be some technical issues you may need to resolve. Report on Piazza ASAP.
 *
 * The single unit test is intended to validate whether the provided model's predictions are as expected or not.
 */
public class __Test12PredictionJob {
  @Rule
  public Timeout timeout = Timeout.millis(10000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  static final float EPS = 0.0001f;

  @Test
  public void testPrediction() {
    DeviceId id1 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID1).build();
    DeviceId id2 = DeviceId.newBuilder().setOs(OsType.ANDROID).setUuid(UUID2).build();
    DeviceId id3 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID3).build();
    DeviceId id4 = DeviceId.newBuilder().setOs(OsType.IOS).setUuid(UUID4).build();

    DoFn<KV<DeviceId, float[]>, PredictionData> doFn =
        Predictions.getPredictDoFn(PathConfigs.ofLocal().getPathToModel());

    Map<DeviceId, KV<Integer, Double>> expected = new ImmutableMap.Builder<DeviceId, KV<Integer, Double>>()//
        .put(id1, KV.of(9, 16.528018951416016d))//
        .put(id2, KV.of(4, 19.154563903808594d))//
        .put(id3, KV.of(7, 13.110543251037598d))//
        .put(id4, KV.of(1, 16.438339233398438d))//
        .build();

    PAssert.that(tp.apply(Create.of(//
        KV.of(id1, PredictionUtils.imageData.get(0)), //
        KV.of(id2, PredictionUtils.imageData.get(1)), //
        KV.of(id3, PredictionUtils.imageData.get(2)), //
        KV.of(id4, PredictionUtils.imageData.get(3)) //
    )).apply(ParDo.of(doFn))).satisfies(out -> {
      Set<DeviceId> verified = new HashSet<>();
      for (PredictionData pd : out) {
        try {
          System.out.println(ProtoUtils.getJsonFromMessage(pd, true)); // <- useful for debugging.
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
        assertTrue(expected.containsKey(DeviceProfileUtils.getCanonicalId(pd.getId())));
        assertEquals(expected.get(DeviceProfileUtils.getCanonicalId(pd.getId())).getValue(), pd.getScore(), EPS);
        assertEquals(expected.get(DeviceProfileUtils.getCanonicalId(pd.getId())).getKey().intValue(),
            pd.getPrediction());
        verified.add(DeviceProfileUtils.getCanonicalId(pd.getId()));
      }
      assertEquals(expected.keySet(), verified);
      return null;
    });
    tp.run();
  }
}
