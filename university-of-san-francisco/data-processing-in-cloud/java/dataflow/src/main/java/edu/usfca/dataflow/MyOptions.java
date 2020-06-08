package edu.usfca.dataflow;

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;

public interface MyOptions extends DataflowPipelineOptions {
  @Description("Job name when running on GCP")
  @Default.String("dummy-this can be anything")
  String getJob();

  void setJob(String job);

  // Default is set true, but when you work on task C, you'll want to specify "--isLocal=false"
  @Description("DirectRunner will be used if true")
  @Default.Boolean(true)
  boolean getIsLocal();

  void setIsLocal(boolean value);

  // If you want to override the path (specified in Main method), then use this flag.
  // "--pathToResourceRoot=gs://hello-world/my-data-large"
  @Description("This will override the path in PathConfigs class.")
  String getPathToResourceRoot();

  void setPathToResourceRoot(String path);

  // "--userCountThreshold=1234"
  @Description("See BidLogJob.")
  @Default.Integer(4)
  int getUserCountThreshold();

  void setUserCountThreshold(int count);

  // "--appCountThreshold=1234"
  @Description("See BidLogJob.")
  @Default.Integer(3)
  int getAppCountThreshold();

  void setAppCountThreshold(int count);

  // "--geoCountThreshold=1234"
  @Description("See BidLogJob.")
  @Default.Integer(8)
  int getGeoCountThreshold();

  void setGeoCountThreshold(int count);

  // "--bidLogCountThreshold=1234"
  @Description("See BidLogJob.")
  @Default.Integer(10)
  int getBidLogCountThreshold();

  void setBidLogCountThreshold(int count);

  // "--exportToBigQuery=true" or "--exportToBigQuery" when needed.
  @Description("See PredictionJob.")
  @Default.Boolean(false)
  boolean getExportToBigQuery();

  void setExportToBigQuery(boolean value);
}
