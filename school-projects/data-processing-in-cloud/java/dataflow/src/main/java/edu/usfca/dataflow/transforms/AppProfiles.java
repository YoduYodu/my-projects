package edu.usfca.dataflow.transforms;

import java.util.HashMap;
import java.util.Map;

import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

import edu.usfca.dataflow.CorruptedDataException;
import edu.usfca.protobuf.Profile.AppProfile;
import edu.usfca.protobuf.Profile.DeviceProfile;

public class AppProfiles {
  public static class ComputeAppProfiles extends PTransform<PCollection<DeviceProfile>, PCollection<AppProfile>> {

    @Override
    public PCollection<AppProfile> expand(PCollection<DeviceProfile> dps) throws CorruptedDataException {
      // Check duplicates
      dps.apply("ExtractDeviceId", MapElements.into(TypeDescriptors.strings()).via(DeviceProfile::getDeviceId))
          .apply(Count.perElement()) //
          .apply("CheckDuplicates", ParDo.of(new DoFn<KV<String, Long>, Void>() {
            @ProcessElement
            public void processElement(ProcessContext c) {
              if (c.element().getValue() > 1L) {
                throw new CorruptedDataException("PCollection<DeviceProfile> contains duplicate DeviceId");
              }
            }
          }));
      // merge AppProfiles
      return dps.apply("Flatten", ParDo.of(new DoFn<DeviceProfile, KV<String, AppProfile>>() {
        @ProcessElement
        public void processElement(ProcessContext c) {
          c.element().getAppList().forEach(appActivity -> {
            Map<Integer, Integer> userCountPerExchange = new HashMap<>();
            for (int i = 0; i < appActivity.getExchangeCount(); i++) {
              userCountPerExchange.put(appActivity.getExchange(i), 1);
            }
            c.output(KV.of(appActivity.getBundle(), //
                AppProfile.newBuilder() //
                    .setBundle(appActivity.getBundle()) //
                    .setUserCount(1) //
                    .putAllUserCountPerExchange(userCountPerExchange) //
                    .build()));
          });
        }
      })).apply("CombineAppProfile", Combine.perKey(appProfiles -> {
        int userCount = 0;
        Map<Integer, Integer> userCountPerExchange = new HashMap<>();
        for (AppProfile appProfile : appProfiles) {
          userCount += appProfile.getUserCount();

          Map<Integer, Integer> appProfileMap = appProfile.getUserCountPerExchangeMap();
          for (int exchange : appProfileMap.keySet()) {
            userCountPerExchange.merge(exchange, appProfileMap.get(exchange), Integer::sum);
          }
        }

        return AppProfile.newBuilder()//
            .setBundle(appProfiles.iterator().next().getBundle())//
            .setUserCount(userCount)//
            .putAllUserCountPerExchange(userCountPerExchange)//
            .build();
      }))//
          .apply(Values.create());
    }
  }
}
