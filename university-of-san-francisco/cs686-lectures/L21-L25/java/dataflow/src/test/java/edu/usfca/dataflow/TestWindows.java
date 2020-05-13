package edu.usfca.dataflow;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.options.PipelineOptions.CheckEnabled;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Sessions;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * The contents of this file will be covered in Lecture 21 & Lecture 22.
 *
 * There is "ungraded homework" below, through which you can check your understanding of windowing in Beam.
 */
public class TestWindows {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  @Rule
  public final transient TestPipeline tp = TestPipeline.create();

  @Before
  public void before() {
    tp.getOptions().setStableUniqueNames(CheckEnabled.OFF);
  }

  static class MyData implements Serializable {
    final private Long eventAt;
    final private String key;

    private MyData(String key, Long eventAt) {
      this.key = key;
      this.eventAt = eventAt;
    }

    static MyData of(String key, Long eventAt) {
      return new MyData(key, eventAt);
    }

    // The following "WARNING" messages may appear if you do not override equals() properly:
    // WARNING: Coder of type class org.apache.beam.sdk.coders.SerializableCoder has a #structuralValue method which
    // does not return true when the encoding of the elements is equal. Element
    // edu.usfca.dataflow.TestWindows$MyData@262b1ea9
    //

    // auto-generated via Guava.
    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      MyData myData = (MyData) o;
      return Objects.equal(eventAt, myData.eventAt) && Objects.equal(key, myData.key);
    }

    // auto-generated via Guava.
    @Override
    public int hashCode() {
      return Objects.hashCode(eventAt, key);
    }
  }

  static List<MyData> getSamples() {
    final long secondToMillis = 1000L;
    final long baseTimestamp = 1000L * 1000L;
    return Arrays.asList(//
        MyData.of("abc", baseTimestamp), // First event.
        MyData.of("abc", baseTimestamp + 30 * secondToMillis + 1L), // 30 secs & 1 millisecond after first event.
        MyData.of("abc", baseTimestamp + 60 * secondToMillis - 1L), // 59 secs and 999 milliseconds after first event.
        MyData.of("abc", baseTimestamp + 60 * secondToMillis)// exactly 60 secs after first event.
    );
  }

  // When an anonymous DoFn is used in unit test methods below, it will result in an error (for Beam).
  // By taking it out into a static class, you can ensure that DoFn instances will be properly serialized (for Beam).
  static class PrintElementWithTimestamp extends DoFn<MyData, Void> {
    @ProcessElement
    public void process(ProcessContext c) {
      LOG.info("key {} with event at {} & Beam's timestamp {}\n", c.element().key, c.element().eventAt,
          c.timestamp().getMillis());
    }
  }

  static class KvPrinter extends PTransform<PCollection<KV<String, Long>>, PDone> {
    final String prefix;

    public KvPrinter() {
      this.prefix = "";
    }

    public KvPrinter(String prefix) {
      this.prefix = prefix;
    }

    @Override
    public PDone expand(PCollection<KV<String, Long>> input) {
      input.apply(ParDo.of(new DoFn<KV<String, Long>, Void>() {
        @ProcessElement
        public void process(ProcessContext c) {
          // "c.timestamp().getMillis()" is the "end" of the window (in unix millis).
          System.out.format("[%s] key %s count %d [window timestamp %d]\n", prefix, c.element().getKey(),
              c.element().getValue(), c.timestamp().getMillis());
        }
      }));
      return PDone.in(input.getPipeline());
    }
  }

  @Test
  public void test_SingleGlobalWindow() {
    // Step 1: Get sample data (this is a bounded PC, as usual!).
    PCollection<MyData> data = tp.apply(Create.of(getSamples()));

    // Step 2: Sanity check.
    PAssert.that(data).satisfies(out -> {
      // Note: "out" is of Iterable<MyData> type.
      assertEquals(4, Iterables.size(out));
      return null;
    });

    // Step 3: This prints out Beam's "intrinsic timestamp" along with element's value.
    // "Beam's timestamp" may differ from runner to runner.
    // Why? see
    // https://beam.apache.org/documentation/programming-guide/#pcollection-characteristics (Section 3.2.5).
    // We covered this in L19.
    data.apply(ParDo.of(new PrintElementWithTimestamp()));

    tp.run();
  }

  // Given any "element" in your PCollection, you can associate it with "intrinsic timestamp".
  // This "timestamp" can come from the element itself (like in this case), or you can get it from somewhere else.
  static class ApplyTimestamp extends DoFn<MyData, MyData> {
    @ProcessElement
    public void process(ProcessContext c) {
      c.outputWithTimestamp(c.element(), org.joda.time.Instant.ofEpochMilli(c.element().eventAt));
    }
  }

  @Test
  public void test_FixedWindow() {
    // Step 1: Get sample data (this is a bounded PC, as usual!).
    PCollection<MyData> data = tp.apply(Create.of(getSamples()));

    // Step 2: Apply Timestamp.
    PCollection<MyData> dataWithTs = data.apply(ParDo.of(new ApplyTimestamp()));

    // Step 3: This prints out Beam's "intrinsic timestamp" along with element's value.
    // Compare this with the output from the earlier unit test.
    dataWithTs.apply(ParDo.of(new PrintElementWithTimestamp()));

    // Step 4: Apply Window transform (Fixed Windows of size 30 secs each).
    // Then, simply apply Count.perKey() where we use "key" of MyData as Key.
    // This "windowing" would result in: the first element in the first window), the next two in the second window, and
    // the fourth in the third.
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(FixedWindows.of(Duration.standardSeconds(30)))).apply(WithKeys
              .of((SerializableFunction<MyData, String>) input -> input.key).withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      PAssert.that(windowedKeys).containsInAnyOrder(KV.of("abc", 1L), KV.of("abc", 1L), KV.of("abc", 2L));

      // Expect to see (order may differ):
      // key abc count 1 [window timestamp 1019999] for window [ 990000, 1020000) (30-second)
      // key abc count 1 [window timestamp 1049999] for window [1020000, 1050000) (30-second)
      // key abc count 2 [window timestamp 1079999] for window [1050000, 1080000) (30-second)
      windowedKeys.apply(new KvPrinter());
    }

    // Step 5: Similar to the above, but let's add an offset of "10 secs & 1 millisecond".
    // This effectively "moves" all windows to the right by 10 seconds & 1 millisecond.
    // As a result, there will be one window (that contains the first element) and another one (with the other three).
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(FixedWindows.of(Duration.standardSeconds(30)).withOffset(Duration.millis(10001))))
          .apply(WithKeys.of((SerializableFunction<MyData, String>) input -> input.key)
              .withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      PAssert.that(windowedKeys).containsInAnyOrder(KV.of("abc", 1L), KV.of("abc", 3L));

      // Expect to see (order may differ):
      // key abc count 1 [window timestamp 1000000] for window [ 960000+10001, 990000+10001) (30-second)
      // key abc count 3 [window timestamp 1060000] for window [1020000+10001, 1050000+10001) (30-second)
      // Notice that there is no output for the window [ 990000+10001, 1020000+10001).
      windowedKeys.apply(new KvPrinter());
    }

    tp.run();
  }

  @Test
  public void test_SlidingWindow() {
    // Step 1: Get sample data (this is a bounded PC, as usual!).
    PCollection<MyData> data = tp.apply(Create.of(getSamples()));

    // Step 2: Apply Timestamp.
    PCollection<MyData> dataWithTs = data.apply(ParDo.of(new ApplyTimestamp()));

    // Step 3: This prints out Beam's "intrinsic timestamp" along with element's value.
    // Compare this with the output from the earlier unit test.
    dataWithTs.apply(ParDo.of(new PrintElementWithTimestamp()));

    // Step 4: Sliding windows of 30 secs (for every 15 secs).
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(SlidingWindows.of(Duration.standardSeconds(30)).every(Duration.standardSeconds(15))))
          .apply(WithKeys.of((SerializableFunction<MyData, String>) input -> input.key)
              .withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // key abc count 1 [window timestamp 1004999]
      // key abc count 1 [window timestamp 1019999]
      // key abc count 1 [window timestamp 1034999]
      // key abc count 1 [window timestamp 1049999]
      // key abc count 2 [window timestamp 1064999]
      // key abc count 2 [window timestamp 1079999]
      windowedKeys.apply(new KvPrinter());
    }

    // Step 5: Sliding windows of 20 secs (for every 12 secs).
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(SlidingWindows.of(Duration.standardSeconds(20)).every(Duration.standardSeconds(12))))
          .apply(WithKeys.of((SerializableFunction<MyData, String>) input -> input.key)
              .withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // key abc count 1 [window timestamp 1003999]
      // key abc count 1 [window timestamp 1015999]
      // key abc count 1 [window timestamp 1039999]
      // key abc count 2 [window timestamp 1063999]
      // key abc count 2 [window timestamp 1075999]
      // Notice that it "skips" empty windows (ending at 1027999 and at 1051999) because they do not contain any
      // elements.
      windowedKeys.apply(new KvPrinter());
    }

    tp.run();
  }

  @Test
  public void test_SessionWindows() {
    // Step 1: Get sample data (this is a bounded PC, as usual!).
    PCollection<MyData> data = tp.apply(Create.of(getSamples()));

    // Step 2: Apply Timestamp.
    PCollection<MyData> dataWithTs = data.apply(ParDo.of(new ApplyTimestamp()));

    // Step 3: This prints out Beam's "intrinsic timestamp" along with element's value.
    // Compare this with the output from the earlier unit test.
    dataWithTs.apply(ParDo.of(new PrintElementWithTimestamp()));

    // Step 4: Session windows with a gap of "30 secs."
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(Sessions.withGapDuration(Duration.standardSeconds(30)))).apply(WithKeys
              .of((SerializableFunction<MyData, String>) input -> input.key).withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [session] key abc count 1 [window timestamp 1029999]
      // [session] key abc count 3 [window timestamp 1089999]
      windowedKeys.apply(new KvPrinter("session30"));
    }

    // Step 5: Session windows with a gap of "29 secs."
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(Sessions.withGapDuration(Duration.standardSeconds(29)))).apply(WithKeys
              .of((SerializableFunction<MyData, String>) input -> input.key).withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [session29] key abc count 1 [window timestamp 1028999]
      // [session29] key abc count 1 [window timestamp 1059000]
      // [session29] key abc count 2 [window timestamp 1088999]
      windowedKeys.apply(new KvPrinter("session29"));
    }

    // Step 6: Session windows with a gap of "1 millisecond."
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(Sessions.withGapDuration(Duration.millis(1)))).apply(WithKeys
              .of((SerializableFunction<MyData, String>) input -> input.key).withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [session1ms] key abc count 1 [window timestamp 1000000]
      // [session1ms] key abc count 1 [window timestamp 1030001]
      // [session1ms] key abc count 1 [window timestamp 1059999]
      // [session1ms] key abc count 1 [window timestamp 1060000]
      windowedKeys.apply(new KvPrinter("session1ms"));
    }
    tp.run();
  }

  // --------- Ungraded Homework ---------

  static List<MyData> getSamplesForHomework() {
    final long secondToMillis = 1000L;
    final long baseTimestamp = 0L;

    // TODO: To change the "eventAt" arguments below to make the console output (of test_homework()) to agree with the
    // comments (for Steps 4-7 at the same time).
    // There are many correct answers, and feel free to share yours on Piazza.
    // It's sufficient to replace "1* 2* 3* 4* 5*" by some numbers you choose.
    return Arrays.asList(//
        MyData.of("A", baseTimestamp + 1 * secondToMillis), //
        MyData.of("A", baseTimestamp + 2 * secondToMillis), //
        MyData.of("A", baseTimestamp + 3 * secondToMillis), //
        MyData.of("B", baseTimestamp + 4 * secondToMillis), //
        MyData.of("B", baseTimestamp + 5 * secondToMillis) //
    );
  }

  @Test
  public void test_homework() {
    // Step 1: Get sample data (this is a bounded PC, as usual!).
    PCollection<MyData> data = tp.apply(Create.of(getSamplesForHomework()));

    // Step 2: Apply Timestamp.
    PCollection<MyData> dataWithTs = data.apply(ParDo.of(new ApplyTimestamp()));

    // Step 3: This prints out Beam's "intrinsic timestamp" along with element's value.
    // Compare this with the output from the earlier unit test.
    dataWithTs.apply(ParDo.of(new PrintElementWithTimestamp()));

    // Step 4: Fixed windows of 30 secs.
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(FixedWindows.of(Duration.standardSeconds(30)))).apply(WithKeys
              .of((SerializableFunction<MyData, String>) input -> input.key).withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [Step4] key A count 1 [window timestamp 29999]
      // [Step4] key A count 2 [window timestamp 59999]
      // [Step4] key B count 2 [window timestamp 59999]
      windowedKeys.apply(new KvPrinter("Step4"));
    }

    // Step 5: Fixed windows of 30 secs with offset of 10 seconds.
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(FixedWindows.of(Duration.standardSeconds(30)).withOffset(Duration.standardSeconds(10))))
          .apply(WithKeys.of((SerializableFunction<MyData, String>) input -> input.key)
              .withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [Step5] key A count 3 [window timestamp 39999]
      // [Step5] key B count 2 [window timestamp 69999]
      windowedKeys.apply(new KvPrinter("Step5"));
    }

    // Step 6: Sliding windows of 30 secs (every 15 secs) with offset of 10 seconds.
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(SlidingWindows.of(Duration.standardSeconds(30)).every(Duration.standardSeconds(15))
              .withOffset(Duration.standardSeconds(10))))
          .apply(WithKeys.of((SerializableFunction<MyData, String>) input -> input.key)
              .withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [Step6] key A count 1 [window timestamp 24999]
      // [Step6] key A count 3 [window timestamp 39999]
      // [Step6] key A count 2 [window timestamp 54999]
      // [Step6] key B count 1 [window timestamp 54999]
      // [Step6] key B count 2 [window timestamp 69999]
      // [Step6] key B count 1 [window timestamp 84999]
      windowedKeys.apply(new KvPrinter("Step6"));
    }

    // Step 7: Sessions with a gap of 10 seconds.
    {
      PCollection<KV<String, Long>> windowedKeys = dataWithTs
          .apply(Window.into(Sessions.withGapDuration(Duration.standardSeconds(10)))).apply(WithKeys
              .of((SerializableFunction<MyData, String>) input -> input.key).withKeyType(TypeDescriptors.strings()))
          .apply(Count.perKey());

      // Expect to see (order may differ):
      // [Step7] key A count 1 [window timestamp 29999]
      // [Step7] key A count 2 [window timestamp 44999]
      // [Step7] key B count 1 [window timestamp 49999]
      // [Step7] key B count 1 [window timestamp 64999]
      windowedKeys.apply(new KvPrinter("Step7"));
    }

    tp.run();
  }

  @Test
  public void test_quizL21() {
    tp.apply(Create.of("abc", "def")).apply(new StrPrinter());
    tp.run();
  }

  static class StrPrinter extends PTransform<PCollection<String>, PDone> {
    @Override
    public PDone expand(PCollection<String> input) {
      input.apply(ParDo.of(new DoFn<String, Void>() {
        @ProcessElement
        public void process(ProcessContext c) {
          System.out.format("key %s [window timestamp %d]\n", c.element(), c.timestamp().getMillis());
        }
      }));
      return PDone.in(input.getPipeline());
    }
  }
}
