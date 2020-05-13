package edu.usfca.dataflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is sample code part 1 for L34.
 */
public class BufferingDoFns {

  private static final Logger LOG = LoggerFactory.getLogger(BufferingDoFns.class);

  /**
   * This DoFn is used to illustrate how the annotated methods in DoFn are executed.
   *
   * Given PC of Integers, this DoFn's job is to only keep the Integers that are equal to the given "target" value.
   *
   * Obviously, we can do this more easily by using "Filter.by", for instance.
   *
   * The goal here is to finish implementing "finishBundle" method to make this DoFn behave correctly.
   *
   * (No need to change any other parts).
   *
   * See the unit tests in "TestBufferingDoFns" for reference.
   */
  public static class SearchTargetWithBuffer extends DoFn<Integer, Integer> {
    final int bufferSize;

    Integer target;
    List<Integer> buffer;

    public SearchTargetWithBuffer(int bufferSize, int target) {
      this.bufferSize = bufferSize;
      this.target = target;
    }

    @Setup
    public void setup() {
      // nothing to do here.
    }

    @StartBundle
    public void startBundle() {
      buffer = new ArrayList<>();
      LOG.info("[start {} {}] initialized buffer of size {}.", //
          Thread.currentThread().getName(), //
          Thread.currentThread().getId(), //
          bufferSize);
    }

    @ProcessElement
    public void process(ProcessContext c) {
      buffer.add(c.element());
      if (buffer.size() >= bufferSize) {
        flush(c);
      }
    }

    @FinishBundle
    public void finishBundle(FinishBundleContext c) {
      // TODO: Implement me!
      LOG.info("[finish {} {}] buffer currently contains {} elements (max size {})", //
          Thread.currentThread().getName(), //
          Thread.currentThread().getId(), //
          buffer.size(), bufferSize);

      // Note that "FinishBundleContext c" can be used to "output" elements here.
      // Example is shown below. The second and third args are meaningless for us (since we're doing batch processing).
      // c.output(null, Instant.EPOCH, GlobalWindow.INSTANCE);
    }

    public void flush(ProcessContext c) {
      LOG.info("[flush {} {}] buffer currently contains {} elements (max size {})", //
          Thread.currentThread().getName(), //
          Thread.currentThread().getId(), //
          buffer.size(), bufferSize);
      for (Integer element : buffer) {
        if (element.equals(target)) {
          c.output(element);
        }
      }
      buffer.clear();
    }
  }
}
