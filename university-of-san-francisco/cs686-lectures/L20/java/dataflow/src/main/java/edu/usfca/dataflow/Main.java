package edu.usfca.dataflow;

import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.metrics.MetricResult;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  // TODO: Make sure you change the following four to your own settings.
  // Future Labs and Projects will be graded by accessing your Dataflow jobs.
  final static String GCP_PROJECT_ID = "beer-spear";
  final static String GCS_BUCKET = "gs://usf-cs686-sp20";
  final static String REGION = "us-west1";
  final static String GIT_ID = "hadenlee";

  static DataflowPipelineOptions getOptions() {
    DataflowPipelineOptions options = PipelineOptionsFactory.create().as(DataflowPipelineOptions.class);

    // This will display the "current settings" stored in options.
    System.out.println(options.toString());

    options.setTempLocation(GCS_BUCKET + "/staging"); // "/staging" was added on Mar 15 around 3pm. Without it, you may see an error on the console.
    options.setJobName(String.format("my-dataflow-job-%s", GIT_ID));
    options.setRunner(DataflowRunner.class);
    options.setMaxNumWorkers(1);
    options.setRegion(REGION);
    options.setProject(GCP_PROJECT_ID);

    // You will see more info here.
    // To run a pipeline (job) on GCP via Dataflow, you need to specify a few things like the ones above.
    System.out.println(options.toString());
    return options;
  }

  public static void main(String[] args) {
    DataflowPipelineOptions options = getOptions();

    Pipeline p = Pipeline.create(options);

    PCollection<String> rawStrings = p.apply("Create PC from in-memory",
        Create.of("a", "abc", "bc", "ac", "cba", "xxyz", "xz", "azx").withCoder(StringUtf8Coder.of()));

    PCollection<String> prefixes = rawStrings.apply("Compute prefixes", ParDo.of(new DoFn<String, String>() {
      @ProcessElement
      public void processElement(@Element String string, OutputReceiver<String> out) {

        Metrics.counter("prefix", "cnt_in").inc();
        Metrics.counter("prefix", "length_total").inc(string.length());
        Metrics.distribution("prefix", "length_dist").update(string.length());

        for (int i = 0; i < string.length(); i++) {
          out.output(string.substring(0, i + 1));
        }
      }
    }));

    PCollection<KV<String, Long>> counted =
        prefixes.apply("Count prefixes", Count.perElement()).apply("Filter popular prefixes",
            Filter.by((SerializableFunction<KV<String, Long>, Boolean>) elem -> elem.getValue() >= 2));

    counted.apply("Print to Console", ParDo.of(new DoFn<KV<String, Long>, Void>() {
      @ProcessElement
      public void processElement(@Element KV<String, Long> prefix) {
        // Where will this message be printed?
        LOG.info("Prefix [{}] Count [{}]\n", prefix.getKey(), prefix.getValue());
      }
    }));

    // Let's transform "$prefix,$count" KV into String so we can write to a plain-text file.
    counted
        .apply(MapElements.into(TypeDescriptors.strings())
            .via((KV<String, Long> kv) -> String.format("%s: %d", kv.getKey(), kv.getValue())))
        .apply(TextIO.write().to(String.format("%s/output-%s", GCS_BUCKET, GIT_ID)));

    // PipelineResult object contains useful info that can be used for various things.
    // For now, we won't be using it yet.
    PipelineResult result = p.run();

    // This means that your "Java program" will wait (and keep polling) until the job is done.
    // Once it's done, the following for-loop will be executed.
    // If your machine loses connection to GCP Dataflow Host (e.g., you lose the Internet connection),
    // then an exception will be thrown.
    result.waitUntilFinish();

    // Check out the final results (metrics) after the job is finished.
    // This will be printed to the console of your local machine.
    // You can also check the same metrics on Dataflow's Web Console:
    // https://console.cloud.google.com/dataflow?project=<your Google Cloud Project ID>
    for (MetricResult<Long> metric : result.metrics().allMetrics().getCounters()) {
      System.out.format("Metric: %s\n", metric.toString());
    }
  }
}
