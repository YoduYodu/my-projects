package edu.usfca.dataflow;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import edu.usfca.dataflow.BufferingDoFns.SearchTargetWithBuffer;

public class TestL34BufferingDoFns {
  @Rule
  public Timeout timeout = Timeout.millis(5000);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  @Test
  public void test1() {
    // A PCollection that contains 0, 6, 8, and 6.
    PCollection<Integer> data = tp.apply(Create.of(new Integer(0), new Integer(6), new Integer(8), new Integer(6)));

    // With buffer size = 1, this is going to pass because it's (logically) equivalent to processing one element at a
    // time.
    PAssert.that(data.apply(ParDo.of(new SearchTargetWithBuffer(1, 6)))).containsInAnyOrder(new Integer(6),
        new Integer(6));

    tp.run();
  }

  @Test
  public void test2() {
    ArrayList<Integer> rawData = new ArrayList<>(), expected = new ArrayList<>();
    for (int i = 0; i <= 10; i++) {
      for (int j = 0; j < i + 1; j++) {
        rawData.add(new Integer(i));
        if (i == 10) {
          expected.add(new Integer(i));
        }
      }
    }
    assertEquals(66, rawData.size());
    // A PCollection that contains (x+1) copies of x where x = [0 .. 10].
    PCollection<Integer> data = tp.apply(Create.of(rawData));

    // TODO With buffer size = 1, this will pass, obviously.

    // Vary the buffer size from 2 to 67 (just pick a random number, and run).
    // For some buffer sizes, this will fail, unless you implemented "finishBundle" correctly.
    // Pay attention to the "LOG" messages which may help you understand exactly how your code is executed by runner.
    // PAssert.that(data.apply(ParDo.of(new SearchTargetWithBuffer(1, 10)))).containsInAnyOrder(expected);

    PAssert.that(data.apply(ParDo.of(new SearchTargetWithBuffer(67, 10)))).containsInAnyOrder(expected);
    // PAssert.that(data.apply(ParDo.of(new SearchTargetWithBuffer(3, 10)))).containsInAnyOrder(expected);
    // PAssert.that(data.apply(ParDo.of(new SearchTargetWithBuffer(4, 10)))).containsInAnyOrder(expected);

    tp.run();
  }
}
