package edu.usfca.dataflow.transforms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.DeviceProfile.AppActivity;

public class SuspiciousIDs {
  private static final Logger LOG = LoggerFactory.getLogger(SuspiciousIDs.class);

  /**
   * This method serves to flag certain users as suspicious.
   *
   * (1) USER_COUNT_THRESHOLD: This determines whether an app is popular or not.
   *
   * Any app that has more than "USER_COUNT_THRESHOLD" unique users is considered popular.
   *
   * Default value is 4 (so, 5 or more users = popular).
   *
   *
   * (2) APP_COUNT_THRESHOLD: If a user (DeviceProfile) contains more than "APP_COUNT_THRESHOLD" unpopular apps,
   *
   * then the user is considered suspicious.
   *
   * Default value is 3 (so, 4 or more unpopular apps = suspicious).
   *
   *
   * (3) GEO_COUNT_THRESHOLD: If a user (DeviceProfile) contains more than "GEO_COUNT_THRESHOLD" unique Geo's,
   *
   * then the user is considered suspicious.
   *
   * Default value is 8 (so, 9 or more distinct Geo's = suspicious).
   *
   * 
   * (4) BID_LOG_COUNT_THRESHOLD: If a user (DeviceProfile) appeared in more than "BID_LOG_COUNT_THRESHOLD" Bid Logs,
   *
   * then the user is considered suspicious (we're not counting invalid BidLogs for this part as it should have been
   * ignored from the beginning).
   *
   * Default value is 10 (so, 11 or more valid BidLogs from the same user = suspicious).
   *
   * 
   * NOTE: When you run your pipelines on GCP, we'll not use the default values for these thresholds (see the document).
   *
   * The default values are mainly for unit tests (so you can easily check correctness with rather small threshold
   * values).
   */

  public static PCollection<String> getSuspiciousIDs(//
      PCollection<DeviceProfile> dps, //
      PCollection<AppProfile> aps, //
      int USER_COUNT_THRESHOLD, //
      int APP_COUNT_THRESHOLD, //
      int GEO_COUNT_THRESHOLD, //
      int BID_LOG_COUNT_THRESHOLD //
  ) {
    LOG.info("[Thresholds] user count {} app count {} geo count {} bid log count {}", USER_COUNT_THRESHOLD,
        APP_COUNT_THRESHOLD, GEO_COUNT_THRESHOLD, BID_LOG_COUNT_THRESHOLD);

    // Process AppProfile as sideInput
    final PCollectionView<List<String>> popularAppListView =
        aps.apply("PopularApps", ParDo.of(new DoFn<AppProfile, String>() {
          @ProcessElement
          public void processElement(ProcessContext c, @Element AppProfile appProfile) {
            if (appProfile.getUserCount() > USER_COUNT_THRESHOLD) {
              c.output(appProfile.getBundle());
            }
          }
        })).apply(View.asList());

    // Get suspicious DeviceIds
    return dps.apply("GetSuspicious", ParDo.of(new DoFn<DeviceProfile, String>() {
      Set<String> popularBundles;

      @ProcessElement
      public void processElement(ProcessContext c, @Element DeviceProfile deviceProfile) {
        if (popularBundles == null) {
          popularBundles = new HashSet<>(c.sideInput(popularAppListView));
        }

        // If a user (DeviceProfile) contains more than "APP_COUNT_THRESHOLD" unpopular apps
        int unpopularAppsCount = 0;
        for (AppActivity appActivity : deviceProfile.getAppList()) {
          if (!popularBundles.contains(appActivity.getBundle())) {
            unpopularAppsCount++;
          }
        }

        if (unpopularAppsCount > APP_COUNT_THRESHOLD) {
          c.output(deviceProfile.getDeviceId());
          return;
        }

        // If a user (DeviceProfile) contains more than "GEO_COUNT_THRESHOLD" unique Geo's
        // Set<DeviceProfile.GeoActivity> uniqueGeo = new HashSet<>(deviceProfile.getGeoList());
        if (deviceProfile.getGeoList().size() > GEO_COUNT_THRESHOLD) {
          c.output(deviceProfile.getDeviceId());
          return;
        }

        // If a user (DeviceProfile) appeared in more than "BID_LOG_COUNT_THRESHOLD" Bid Logs
        int bidLogCount = 0;
        for (AppActivity appActivity : deviceProfile.getAppList()) {
          for (int count : appActivity.getCountList()) {
            bidLogCount += count;
          }
        }
        if (bidLogCount > BID_LOG_COUNT_THRESHOLD) {
          c.output(deviceProfile.getDeviceId());
        }
      }
    }).withSideInputs(popularAppListView));

  }
}
