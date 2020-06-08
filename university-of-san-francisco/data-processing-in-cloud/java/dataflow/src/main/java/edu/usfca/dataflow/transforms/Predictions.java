package edu.usfca.dataflow.transforms;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.windowing.GlobalWindow;
import org.apache.beam.sdk.values.KV;
import org.joda.time.Instant;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

import edu.usfca.dataflow.utils.BidLogUtils;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Data.PredictionData;

public class Predictions {

  /**
   * This method will be called by the unit tests.
   * <p>
   * The reason for having this method (instead of instantiating a specific DoFn) is to allow you to easily experiment
   * with different implementations of PredictDoFn.
   * <p>
   * The provided code (see below) for "PredictDoFnNever" is "correct" but extremely inefficient.
   * <p>
   * Use it as a reference to implement "PredictDoFn" instead.
   * <p>
   * When you are ready to optimize it, you'll find the ungraded homework for Lab 09 useful (as well as sample code from
   * L34: DF-TF).
   */
  public static DoFn<KV<String, float[]>, PredictionData> getPredictDoFn(String pathToModelDir) {
    return new PredictDoFn(pathToModelDir);
  }

  // This utility method simply returns the index with largest prediction score.
  // Input must be an array of length 10.
  static int getArgMax(float[] pred) {
    int prediction = -1;
    for (int j = 0; j < 10; j++) {
      if (prediction == -1 || pred[prediction] < pred[j]) {
        prediction = j;
      }
    }
    return prediction;
  }

  static class PredictDoFn extends DoFn<KV<String, float[]>, PredictionData> {
    final static String tfTag = "serve";
    final String pathToModelDir;

    transient Tensor rate;
    transient SavedModelBundle mlBundle;

    List<KV<String, float[]>> buffer;

    final static int BUFFER_MAX_SIZE = 45;
    float[][] batchPrediction = new float[BUFFER_MAX_SIZE][28 * 28];

    public PredictDoFn(String pathToModelDir) {
      this.pathToModelDir = pathToModelDir;
    }

    float[][] getPrediction(float[][] inputFeatures, SavedModelBundle mlBundle) {
      float[][] prediction = new float[BUFFER_MAX_SIZE][10];
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
      mlBundle = SavedModelBundle.load(pathToModelDir, tfTag);
      float[] keep_prob_arr = new float[1024];
      Arrays.fill(keep_prob_arr, 1f);
      rate = Tensor.create(new long[] {1, 1024}, FloatBuffer.wrap(keep_prob_arr));
    }

    @StartBundle
    public void start() {
      buffer = new ArrayList<>();
    }

    @ProcessElement
    public void process(ProcessContext c) {
      buffer.add(c.element());
      if (buffer.size() >= BUFFER_MAX_SIZE) {
        for (int i = 0; i < BUFFER_MAX_SIZE; i++) {
          batchPrediction[i] = buffer.get(i).getValue();
        }

        float[][] pred = getPrediction(batchPrediction, mlBundle);
        for (int i = 0; i < BUFFER_MAX_SIZE; i++) {
          int prediction = getArgMax(pred[i]);
          DeviceId deviceId = DeviceId.newBuilder() //
              .setOs(BidLogUtils.getOs(buffer.get(i).getKey().substring(0, 1))) //
              .setUuid(buffer.get(i).getKey().substring(1)) //
              .build();
          c.output(PredictionData.newBuilder() //
              .setId(deviceId) //
              .setPrediction(prediction) //
              .setScore(pred[i][prediction]) //
              .build());
        }
        buffer.clear();
      }
    }

    @FinishBundle
    public void finish(FinishBundleContext c) {
      for (int i = 0; i < buffer.size(); i++) {
        batchPrediction[i] = buffer.get(i).getValue();
      }

      float[][] pred = getPrediction(batchPrediction, mlBundle);

      for (int i = 0; i < buffer.size(); i++) {
        int prediction = getArgMax(pred[i]);
        DeviceId deviceId = DeviceId.newBuilder() //
            .setOs(BidLogUtils.getOs(buffer.get(i).getKey().substring(0, 1))) //
            .setUuid(buffer.get(i).getKey().substring(1)) //
            .build();
        c.output(PredictionData.newBuilder() //
            .setId(deviceId) //
            .setPrediction(prediction) //
            .setScore(pred[i][prediction]) //
            .build(), Instant.EPOCH, GlobalWindow.INSTANCE);
      }
      buffer.clear();
    }
  }
}
