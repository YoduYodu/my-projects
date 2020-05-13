package edu.usfca.dataflow;


import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableReference;

import edu.usfca.dataflow.jobs1.BidLogJob;
import edu.usfca.dataflow.jobs2.PredictionJob;

public class Main {
  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public final static String GCP_PROJECT_ID = "effortless-uncomplication";
  public final static String GCS_BUCKET = "gs://test_bucket_l20_cs686";
  public final static String REGION = "us-west1";

  public final static String MY_BQ_DATASET = "cs686_proj5";
  public final static String MY_BQ_TABLE = "my_proj5_table";

  public final static TableReference DEST_TABLE =
      new TableReference().setProjectId(GCP_PROJECT_ID).setDatasetId(MY_BQ_DATASET).setTableId(MY_BQ_TABLE);

  public final static String LOCAL_PATH_TO_RESOURCE_DIR =
      "/Users/tong/Downloads/project5";

  public final static String GCS_PATH_TO_RESOURCE_DIR = GCS_BUCKET + "/resources/project5-small-data";

  public static void main(String[] args) {
    MyOptions options = PipelineOptionsFactory.fromArgs(args).as(MyOptions.class);

    final String job = options.getJob();
    switch (job) {
      case "bidLogJob": // Pipeline #1 for Task A
        options = setDefaultValues(options);
        BidLogJob.execute(options);
        break;

      case "predictionJob": // Pipeline #2 for Task B
        options = setDefaultValues(options);
        PredictionJob.execute(options);
        break;

      default: // Should not be reached.
        System.out.println("unknown job flag: " + job);
        break;
    }
  }

  /**
   * This sets default values for the Options class so that when you run your job on GCP, it won't complain about
   * missing parameters.
   */
  static MyOptions setDefaultValues(MyOptions options) {
    System.out.format("user.dir: %s", System.getProperty("user.dir"));

    options.setJobName(String.format("%s-%05d", options.getJob(), org.joda.time.Instant.now().getMillis() % 100000));

    options.setTempLocation(GCS_BUCKET + "/staging");
    if (options.getIsLocal()) {
      options.setRunner(DirectRunner.class);
    } else {
      options.setRunner(DataflowRunner.class);
    }
    if (options.getMaxNumWorkers() == 0) {
      options.setMaxNumWorkers(1);
    }
    if (StringUtils.isBlank(options.getWorkerMachineType())) {
      options.setWorkerMachineType("n1-standard-1");
    }
    options.setDiskSizeGb(150);
    options.setRegion(REGION);
    options.setProject(GCP_PROJECT_ID);

    // You will see more info here.
    // To run a pipeline (job) on GCP via Dataflow, you need to specify a few things like the ones above.
    LOG.info(options.toString());

    return options;
  }
}
