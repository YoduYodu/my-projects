package edu.usfca.dataflow.transforms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.usfca.dataflow.CorruptedDataException;
import edu.usfca.dataflow.utils.PredictionUtils;
import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Common.DeviceId;
import edu.usfca.protobuf.Profile.DeviceProfile;
import edu.usfca.protobuf.Profile.InAppPurchaseProfile;

public class Features {
  /**
   * This PTransform takes a PCollectionList that contains three PCollections of Strings.
   *
   * 1. DeviceProfile (output from the first pipeline) with unique DeviceIDs,
   *
   * 2. DeviceId (output from the first pipeline) that are "suspicious" (call this SuspiciousIDs), and
   *
   * 3. InAppPurchaseProfile (separately provided) with unique bundles.
   *
   * All of these proto messages are Base64-encoded (you can check ProtoUtils class for how to decode that, e.g.).
   *
   * [Step 1] First, in this PTransform, you must filter out (remove) DeviceProfiles whose DeviceIDs are found in the
   * SuspiciousIDs as we are not going to consider suspicious users.
   *
   * [Step 2] Next, you ALSO filter out (remove) DeviceProfiles whose DeviceID's UUIDs are NOT in the following form:
   *
   * ???????0-????-????-????-????????????
   *
   * Effectively, this would "sample" the data at rate (1/16). This sampling is mainly for efficiency reasons (later
   * when you run your pipeline on GCP, the input data is quite large as you will need to make "predictions" for
   * millions of DeviceIDs).
   *
   * To be clear, if " ...getUuid().charAt(7) == '0' " is true, then you process the DeviceProfile; otherwise, ignore
   * it.
   *
   * [Step 3] Then, for each user (DeviceProfile), use the method in
   * {@link edu.usfca.dataflow.utils.PredictionUtils#getInputFeatures(DeviceProfile, Map)} to obtain the user's
   * "Features" (to be used for TensorFlow model). See the comments for this method.
   *
   * Note that the said method takes in a Map (in addition to DeviceProfile) from bundles to IAPP, and thus you will
   * need to figure out how to turn PCollection into a Map. We have done this in the past (in labs & lectures).
   *
   */
  public static class GetInputToModel extends PTransform<PCollectionList<String>, PCollection<KV<String, float[]>>> {

    @Override
    public PCollection<KV<String, float[]>> expand(PCollectionList<String> pcList) {
      /* Turn InAppPurchaseProfile into Map */
      final PCollectionView<Map<String, InAppPurchaseProfile>> bundle2IAPP = pcList.get(2)
          .apply("Bundle:InAppPurchaseProfile", ParDo.of(new DoFn<String, KV<String, InAppPurchaseProfile>>() {
            @ProcessElement
            public void processElement(ProcessContext c, @Element String bundle) {
              try {
                InAppPurchaseProfile inAppPurchaseProfile =
                    ProtoUtils.decodeMessageBase64(InAppPurchaseProfile.parser(), bundle);
                c.output(KV.of(inAppPurchaseProfile.getBundle(), inAppPurchaseProfile));
              } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
              }
            }
          })).apply(View.asMap());

      /* Filter out DeviceProfiles */
      // First, get suspicious PC<DeviceId> as ListView after checking duplicates DeviceId.
      final PCollection<String> suspiciousDeviceIdPC =
          pcList.get(1).apply("SuspiciousDeviceId", ParDo.of(new DoFn<String, String>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
              try {
                DeviceId deviceId = ProtoUtils.decodeMessageBase64(DeviceId.parser(), c.element());
                c.output(String.format("%d%s", deviceId.getOsValue(), deviceId.getUuid()));
              } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
              }
        }
      }));
      // Check duplicate deviceId
      suspiciousDeviceIdPC.apply("CountDeviceId", Count.perElement()) //
          .apply("CheckDuplicates", ParDo.of(new DoFn<KV<String, Long>, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c, @Element KV<String, Long> deviceId2Count) {
              if (deviceId2Count.getValue() > 1) {
                throw new CorruptedDataException("Duplicate DeviceId");
              }
            }
          }));
      final PCollectionView<List<String>> suspiciousAppsView = suspiciousDeviceIdPC.apply(View.asList());

      // Second, get PC<DeviceProfile>
      PCollection<DeviceProfile> deviceProfilePC =
          pcList.get(0).apply("DeviceProfile@Base64", ParDo.of(new DoFn<String, DeviceProfile>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
              try {
                c.output(ProtoUtils.decodeMessageBase64(DeviceProfile.parser(), c.element()));
              } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
              }
            }
          }));
      // Check duplicate DeviceId in DeviceProfile
      deviceProfilePC
          .apply("ExtractDeviceId", MapElements.into(TypeDescriptors.strings()).via(DeviceProfile::getDeviceId))
          .apply(Count.perElement()) //
          .apply("CheckDuplicates", ParDo.of(new DoFn<KV<String, Long>, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
              if (c.element().getValue() > 1L) {
                throw new CorruptedDataException("PCollection<DeviceProfile> contains duplicate DeviceId");
              }
            }
          }));

      return deviceProfilePC.apply("FilterDP#SuspiciousId", ParDo.of(new DoFn<DeviceProfile, DeviceProfile>() {
        Set<String> suspiciousDeviceIdSet;

        @ProcessElement
        public void processElement(ProcessContext c, @Element DeviceProfile deviceProfile) {
          if (suspiciousDeviceIdSet == null) {
            suspiciousDeviceIdSet = new HashSet<>(c.sideInput(suspiciousAppsView));
          }
          if (!suspiciousDeviceIdSet.contains(deviceProfile.getDeviceId())
              && deviceProfile.getDeviceId().charAt(8) == '0') {
            c.output(deviceProfile);
          }
        }
      }).withSideInputs(suspiciousAppsView))
          // Third, get features
          .apply("Predictions", ParDo.of(new DoFn<DeviceProfile, KV<String, float[]>>() {
            Map<String, InAppPurchaseProfile> bundle2IAPPMap;

            @ProcessElement
            public void processElement(ProcessContext c, @Element DeviceProfile deviceProfile) {
              if (bundle2IAPPMap == null) {
                bundle2IAPPMap = new HashMap<>(c.sideInput(bundle2IAPP));
              }
              c.output(
                  KV.of(deviceProfile.getDeviceId(),
                      PredictionUtils.getInputFeatures(deviceProfile, bundle2IAPPMap)));
            }
          }).withSideInputs(bundle2IAPP));

    }
  }
}
