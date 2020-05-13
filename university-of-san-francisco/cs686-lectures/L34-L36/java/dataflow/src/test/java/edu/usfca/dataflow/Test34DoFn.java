package edu.usfca.dataflow;

import java.util.ArrayList;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * This is an illustration of how static variables (of DoFn) that are not thread-safe may lead to incorrect results.
 *
 * GSON's JsonParser (we used in Lab 9, for instance) is thread-safe, so it can be safely contained in a static
 * variable. Here, "Integer" which is not thread-safe is used to demonstrate how that can lead to an incorrect result.
 */
public class Test34DoFn {
  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  int n = 150;

  @Test
  public void testBad() {
    ArrayList<Integer> data = new ArrayList<>();
    int expected = 0;
    for (int i = 0; i < n; i++) {
      data.add(i + 1);
      expected += (i) * (i + 2);
    }

    PAssert.that(tp.apply(Create.of(data)).apply(ParDo.of(new MyDoFnBad())).apply(Combine.globally(Sum.ofIntegers())))
        .containsInAnyOrder(expected);

    tp.run();
  }

  // For each element "z", this DoFn should output (z+1) of (z-1)'s.
  // Hence, if z = 2, then it should output three 1's.
  static class MyDoFnBad extends DoFn<Integer, Integer> {
    static Integer var = new Integer(0);

    @ProcessElement
    public void process(ProcessContext c) {
      for (int i = 0; i < c.element() + 1; i++) {
        var = c.element() - 1;
        int x = var;
        c.output(x);
        if (x + 1 != c.element()) {
          System.out.format("%s %3d %3d\n", this.getClass().getName(), c.element(), x);
        }
      }
    }
  }

  @Test
  public void testNotSoGood() {
    ArrayList<Integer> data = new ArrayList<>();
    int expected = 0;
    for (int i = 0; i < n; i++) {
      data.add(i + 1);
      expected += (i) * (i + 2);
    }

    PAssert
        .that(
            tp.apply(Create.of(data)).apply(ParDo.of(new MyDoFnNotSoGood())).apply(Combine.globally(Sum.ofIntegers())))
        .containsInAnyOrder(expected);

    tp.run();
  }

  // This is "correct" but it comes at a huge cost -- it is no longer fully parallel.
  // In general, this is bad practice against the Beam/Dataflow model.
  static class MyDoFnNotSoGood extends DoFn<Integer, Integer> {
    static Integer var = new Integer(0);
    final static Object lock = new Object();

    @ProcessElement
    public void process(ProcessContext c) {
      for (int i = 0; i < c.element() + 1; i++) {
        synchronized (lock) { // TODO: Why changing this to "synchronized (var)" would NOT work as intended? ;-)
          var = c.element() - 1;
          int x = var;
          c.output(x);
          if (x + 1 != c.element()) {
            System.out.format("%s %3d %3d\n", this.getClass().getName(), c.element(), x);
          }
        }
      }
    }
  }
}
