package edu.usfca.dataflow;

import static edu.usfca.dataflow.TestL34TFHelper.imageData;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.metrics.DistributionResult;
import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.google.common.collect.Iterables;

import edu.usfca.dataflow.PredictDoFns.PredictKindaFast;
import edu.usfca.dataflow.PredictDoFns.PredictOk;
import edu.usfca.dataflow.PredictDoFns.PredictSlow;

public class TestL34TF {
  // TODO: Change this string literal to point to your local directory,
  // which should contain "model" directory in which model files are found.
  // You will do the same in project 5 (and it's the same model from Lab 09).
  final static String PATH_TO_ROOT = "/Users/haden/usf/resources/project5-actual"; // <- TODO Change this!

  final static String PATH_TO_MODEL = PATH_TO_ROOT + "/model"; // <- DO NOT CHANGE THIS.

  public static float[] getBaseData(String uuid) {
    return imageData.get(uuid.toLowerCase().charAt(0) % 15);
  }

  public static final String UUID10 = "3fce9020-1f71-4edc-a662-cbc5fcf868e4";
  public static final String UUID11 = "bfce9020-1f71-4edc-a662-cbc5fcf868e4";
  public static final String UUID12 = "2fce9020-1f71-4edc-a662-cbc5fcf868e4";
  public static final String UUID13 = "afce9020-1f71-4edc-a662-cbc5fcf868e4";

  @Rule
  public Timeout timeout = Timeout.millis(60000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  void testHelper(DoFn<KV<String, float[]>, String> doFn, int MULT) {
    final long testBeginMillis = Instant.now().getMillis();
    List<String> uuids = new ArrayList<>();

    for (int i = 0; i < MULT; i++) {
      uuids.add(UUID10);
      uuids.add(UUID11);
      uuids.add(UUID12);
      uuids.add(UUID13);
    }
    // Overall, making input data with 4 * MULT elements.
    PCollection<String> output = tp.apply(Create.of(uuids))
        .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(float[].class)))
            .via((String uuid) -> KV.of(uuid, getBaseData(uuid))))
        .apply(ParDo.of(doFn));

    PAssert.that(output).satisfies(results -> {
      assertEquals(MULT * 4, Iterables.size(results));
      return null;
    });

    PipelineResult res = tp.run();

    DistributionResult dist = res.metrics().allMetrics().getDistributions().iterator().next().getCommitted();

    final long testEndMillis = Instant.now().getMillis();

    // This is what's reported from the DoFn, so it's more accurate.
    // However, your JVM is likely using multiple threads, so you can't really compare this "average" duration
    // versus the average duration printed in the next line (you need to factor in the number of threads used).
    System.out.format("[distribution] count: %d average: %.3f secs (min %.3f max %.3f)\n", dist.getCount(),
        (float) dist.getSum() / dist.getCount() / 1000.f, dist.getMin() / 1000.f, dist.getMax() / 1000.f);

    // Note that this includes the time to construct the initial input data.
    System.out.format("[test] elapsed time total: %.3f secs (average %.3f secs)\n", //
        (testEndMillis - testBeginMillis) / 1000.f, //
        (testEndMillis - testBeginMillis) / 1000.f / MULT / 4.f);

  }

  @Test
  public void testSlow() {
    // TODO: Run this first!
    // Vary MULT from {1, 5, 10, 20, 30, 40}, and see how the running time grows (final lines of console output).
    final int MULT = 10;
    testHelper(new PredictSlow(PATH_TO_MODEL), MULT);
  }

  @Test
  public void testOk() {
    // TODO: Run this next and compare with testSlow().
    // Vary MULT from {1, 5, 10, 20, 30, 40}, and see how the running time grows (final lines of console output).
    final int MULT = 10;

    // TODO Compare that to "testSlow()", especially when MULT=40.
    // TODO What's the main difference between the two DoFns?

    // Not much has changed, but why is this one much faster?
    // (Recall the discussions from L30 & L34)
    testHelper(new PredictOk(PATH_TO_MODEL), MULT);
  }

  @Test
  public void testFast() {
    final int MULT = 10;

    // TODO - first, implement "PredictKindaFast" with buffer size 200.
    // That is, make (up to) 200 predictions at a time.

    // TODO Compare this to "testOk()", especially when MULT=10 or MULT=20.
    // Surprisingly, testOk() would be faster or comparable.
    // Why? Because "PredictKindaFast" keeps everything in the buffer (for each bundle).
    // That's obviously suboptimal (besides the fact that you do not know how many elements are in a bundle a priori).
    // Fix it by using a reasonably small buffer size (perhaps 20), and flush whenever it gets full.

    // TODO Once you fix this, do you observe that testFast() is substantially faster than testOk(), for MULT=40 or 50?
    // (If not, what do you think is the issue, and how can you measure things better?)
    testHelper(new PredictKindaFast(PATH_TO_MODEL), MULT);
  }
}
