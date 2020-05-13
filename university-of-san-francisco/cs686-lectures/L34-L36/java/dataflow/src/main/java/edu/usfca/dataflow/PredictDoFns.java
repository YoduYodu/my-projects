package edu.usfca.dataflow;

import java.nio.FloatBuffer;
import java.util.Arrays;

import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.joda.time.Instant;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

/**
 * This is sample code part 2 for L34.
 */
public class PredictDoFns {

  public static class PredictSlow extends DoFn<KV<String, float[]>, String> {
    final static String tfTag = "serve"; // <- Do not change this.
    final String pathToModelDir;

    transient Tensor rate;
    transient SavedModelBundle mlBundle;

    Distribution elapsed = Metrics.distribution(this.getClass(), "elapsed");

    public PredictSlow(String pathToModelDir) {
      this.pathToModelDir = pathToModelDir;
    }

    // This method is provided for your convenience. Use it as a reference.
    // "inputFeatures" (float[][]) is assumed to be of size "1" by "768".
    // Note: Tensor<> objects are resources that must be explicitly closed to prevent memory leaks.
    // That's why you are seeing the try blocks below (with that, those resources are auto-closed).
    float[][] getPrediction(float[][] inputFeatures, SavedModelBundle mlBundle) {
      // "prediction" array will store the scores returned by the model (recall that the model returns 10 scores per
      // input).
      float[][] prediction = new float[1][10];
      try (Tensor<?> x = Tensor.create(inputFeatures)) {
        try (Tensor<?> output = mlBundle.session().runner().feed("input_tensor", x).feed("dropout/keep_prob", rate)
            .fetch("output_tensor").run().get(0)) {
          output.copyTo(prediction);
        }
      }
      return prediction;
    }

    @ProcessElement
    public void process(ProcessContext c) {
      final long beginAtMillis = org.joda.time.Instant.now().getMillis();
      // --------------------------------------------------------------------------------
      // Loading the model:
      mlBundle = SavedModelBundle.load(pathToModelDir, tfTag);

      // This is necessary because the model expects to be given this additional tensor.
      // (For those who're familiar with NNs, keep_prob is 1 - dropout.)
      float[] keep_prob_arr = new float[1024];
      Arrays.fill(keep_prob_arr, 1f);
      rate = Tensor.create(new long[] {1, 1024}, FloatBuffer.wrap(keep_prob_arr));

      // --------------------------------------------------------------------------------

      // Prepare the input data (to be fed to the model).
      final String id = c.element().getKey();
      final float[][] inputData = new float[][] {c.element().getValue()};

      // Obtain the prediction scores from the model, and Find the index with maximum score (ties broken by favoring
      // smaller index).
      float[][] pred = getPrediction(inputData, mlBundle);
      int prediction = getArgMax(pred[0]);

      // Build PredictionData proto and output it.
      c.output(String.format("%s,%d,%.6f", id, prediction, pred[0][prediction]));
      elapsed.update(Instant.now().getMillis() - beginAtMillis);
    }
  }

  public static class PredictOk extends DoFn<KV<String, float[]>, String> {
    final static String tfTag = "serve"; // <- Do not change this.
    final String pathToModelDir;

    transient Tensor rate;
    transient SavedModelBundle mlBundle;

    Distribution elapsed = Metrics.distribution(this.getClass(), "elapsed");

    public PredictOk(String pathToModelDir) {
      this.pathToModelDir = pathToModelDir;
    }

    // This method is provided for your convenience. Use it as a reference.
    // "inputFeatures" (float[][]) is assumed to be of size "1" by "768".
    // Note: Tensor<> objects are resources that must be explicitly closed to prevent memory leaks.
    // That's why you are seeing the try blocks below (with that, those resources are auto-closed).
    float[][] getPrediction(float[][] inputFeatures, SavedModelBundle mlBundle) {
      // "prediction" array will store the scores returned by the model (recall that the model returns 10 scores per
      // input).
      float[][] prediction = new float[1][10];
      try (Tensor<?> x = Tensor.create(inputFeatures)) {
        try (Tensor<?> output = mlBundle.session().runner().feed("input_tensor", x).feed("dropout/keep_prob", rate)
            .fetch("output_tensor").run().get(0)) {
          output.copyTo(prediction);
        }
      }
      return prediction;
    }

    @Setup
    public void setup() {
      // This will be executed only once per DoFn instance.
      // --------------------------------------------------------------------------------
      // Loading the model:
      mlBundle = SavedModelBundle.load(pathToModelDir, tfTag);

      // This is necessary because the model expects to be given this additional tensor.
      // (For those who're familiar with NNs, keep_prob is 1 - dropout.)
      float[] keep_prob_arr = new float[1024];
      Arrays.fill(keep_prob_arr, 1f);
      rate = Tensor.create(new long[] {1, 1024}, FloatBuffer.wrap(keep_prob_arr));
    }

    @ProcessElement
    public void process(ProcessContext c) {
      final long beginAtMillis = org.joda.time.Instant.now().getMillis();
      // --------------------------------------------------------------------------------

      // Prepare the input data (to be fed to the model).
      final String id = c.element().getKey();
      final float[][] inputData = new float[][] {c.element().getValue()};

      // Obtain the prediction scores from the model, and Find the index with maximum score (ties broken by favoring
      // smaller index).
      float[][] pred = getPrediction(inputData, mlBundle);
      int prediction = getArgMax(pred[0]);

      // Build PredictionData proto and output it.
      c.output(String.format("%s,%d,%.6f", id, prediction, pred[0][prediction]));
      elapsed.update(Instant.now().getMillis() - beginAtMillis);
    }
  }

  public static class PredictKindaFast extends DoFn<KV<String, float[]>, String> {
    final static String tfTag = "serve"; // <- Do not change this.
    final String pathToModelDir;

    transient Tensor rate;
    transient SavedModelBundle mlBundle;

    final static int BUFFER_MAX_SIZE = 200; // hard-coded. see comments in the unit tests (TestL34TF)
    float[][] batchPrediction = new float[BUFFER_MAX_SIZE][10]; // <- To store prediction results (output of TF model).

    public PredictKindaFast(String pathToModelDir) {
      this.pathToModelDir = pathToModelDir;
    }

    // This makes BUFFER_MAX_SIZE predictions at a time.
    float[][] getPrediction(float[][] inputFeatures, SavedModelBundle mlBundle) {
      try (Tensor<?> x = Tensor.create(inputFeatures)) {
        try (Tensor<?> output = mlBundle.session().runner().feed("input_tensor", x).feed("dropout/keep_prob", rate)
            .fetch("output_tensor").run().get(0)) {
          output.copyTo(batchPrediction);
        }
      }
      return batchPrediction;
    }

    // This will be called exactly once per DoFn instance.
    @Setup
    public void setup() {
      // --------------------------------------------------------------------------------
      // Loading the model:
      mlBundle = SavedModelBundle.load(pathToModelDir, tfTag);

      // This is necessary because the model expects to be given this additional tensor.
      // (For those who're familiar with NNs, keep_prob is 1 - dropout.)
      float[] keep_prob_arr = new float[1024];
      Arrays.fill(keep_prob_arr, 1f);
      rate = Tensor.create(new long[] {1, 1024}, FloatBuffer.wrap(keep_prob_arr));
    }

    // This will be called exactly once per bundle, before the first call to @process.
    @StartBundle
    public void startBundle() {
      // TODO: Implement this!
    }

    @ProcessElement
    public void process(ProcessContext c) {
      // TODO: Implement this!
    }

    // This will be called exactly once per bundle, after the last call to @process.
    @FinishBundle
    public void finishBundle(FinishBundleContext c) {
      // TODO: Implement this! -- Finish "BufferingDoFns" first so you know exactly what to do here.
      // You may find it easier to work on the sample in "BufferingDoFns" class first,
      // and then working on this.

      // Note that "FinishBundleContext c" can be used to "output" elements here.
      // Example is shown below. The second and third args are meaningless for us (since we're doing batch processing).
      // c.output(null, Instant.EPOCH, GlobalWindow.INSTANCE);
    }
  }

  // This utility method simply returns the index with largest prediction score.
  // Input must be an array of length 10.
  // This is provided for you (see PredictDoFnNever" to understand how it's used).
  static int getArgMax(float[] pred) {
    int prediction = -1;
    for (int j = 0; j < 10; j++) {
      if (prediction == -1 || pred[prediction] < pred[j]) {
        prediction = j;
      }
    }
    return prediction;
  }
}
