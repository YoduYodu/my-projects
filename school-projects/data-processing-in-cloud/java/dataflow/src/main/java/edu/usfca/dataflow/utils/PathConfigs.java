package edu.usfca.dataflow.utils;

import org.apache.commons.lang3.StringUtils;

import edu.usfca.dataflow.Main;
import edu.usfca.dataflow.MyOptions;

/**
 * For BidLogJob and PredictionJob, these paths are used to read from / write to data, depending on whether you are
 * running your pipeline locally or not.
 *
 * You are not supposed to change any of these, unless your local settings prevent you from running your jobs.
 */
public class PathConfigs {
  private final String PATH_TO_RESOURCE_ROOT;

  private PathConfigs(String absolutePath) {
    this.PATH_TO_RESOURCE_ROOT = absolutePath;
  }

  public static PathConfigs of(MyOptions options) {
    if (!StringUtils.isBlank(options.getPathToResourceRoot())) {
      return new PathConfigs(options.getPathToResourceRoot());
    }
    return new PathConfigs(options.getIsLocal() ? Main.LOCAL_PATH_TO_RESOURCE_DIR : Main.GCS_PATH_TO_RESOURCE_DIR);
  }

  public static PathConfigs ofLocal() {
    return new PathConfigs(Main.LOCAL_PATH_TO_RESOURCE_DIR);
  }

  public String getReadPathToBidLog() {
    return PATH_TO_RESOURCE_ROOT + "/input/bidlog*.tfrecord.gz";
  }

  public String getWritePathToDeviceProfile() {
    return PATH_TO_RESOURCE_ROOT + "/output/device-profile/result";
  }

  public String getReadPathToDeviceProfile() {
    return PATH_TO_RESOURCE_ROOT + "/output/device-profile/result*";
  }

  public String getWritePathToAppProfile() {
    return PATH_TO_RESOURCE_ROOT + "/output/app-profile/result";
  }

  public String getWritePathToSuspiciousUser() {
    return PATH_TO_RESOURCE_ROOT + "/output/suspicious-user/result";
  }

  public String getReadPathToSuspiciousUser() {
    return PATH_TO_RESOURCE_ROOT + "/output/suspicious-user/result*";
  }

  public String getReadPathToIAPP() {
    return PATH_TO_RESOURCE_ROOT + "/input/iapp*.txt";
  }

  public String getPathToModel() {
    return PATH_TO_RESOURCE_ROOT + "/model";
  }

  public String getWritePathToPredictionData() {
    return PATH_TO_RESOURCE_ROOT + "/output/prediction-data/result";
  }
}
