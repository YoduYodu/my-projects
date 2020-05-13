package edu.usfca.dataflow;

import java.io.File;
import java.util.List;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Distinct;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.Keys;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.ProcessFunction;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

import edu.usfca.dataflow.utils.ProtoUtils;
import edu.usfca.protobuf.Common.StructuredData;

public class Main {

  // Code used for lecture 07.
  public static void main(String[] args) {

    runSamplePipeline2Again();
  }

  static void runSamplePipeline2Again() {
    // 1. Create a pipeline object and set things up.
    Pipeline p = Pipeline.create();

    // 2. Suppose we're reading CSV (text) data from some data source.
    // If you have trouble accessing the file, check the absolute path printed to the console.
    System.out.println((new File("dataflow/resources/sample.csv")).getAbsolutePath());
    PCollection<String> inputData = p.apply(TextIO.read().from("dataflow/resources/sample.csv"));
    // If you print this out to the console, you'll see 14 lines (last "blank/empty" line is ignored by TextIO).
    // inputData.apply(ParDo.of(new printStrToConsole("[input]")));

    // 3. Filter blank string or non-CSV strings, and parse CSV into more structured data.
    PCollection<StructuredData> cleanData = inputData.apply(ParDo.of(new DoFn<String, StructuredData>() {
      @ProcessElement
      public void process(@Element String input, OutputReceiver<StructuredData> out) {
        if (StringUtils.isBlank(input)) {
          return;
        }
        try {
          List<CSVRecord> records = CSVParser.parse(input, CSVFormat.RFC4180).getRecords();
          if (records.size() != 1 || records.get(0).size() != 2) {
            return;
          }

          out.output(
              StructuredData.newBuilder().setDeviceId(records.get(0).get(0)).setAppId(records.get(0).get(1)).build());
        } catch (Exception e) {
          return;
        }
      }
    }));
    // After cleaning, you will see 11 lines (elements) here.
    // Note that "haden-iPhone","Weather" is found twice (the CSV file intentionally contains duplicates).
    cleanData.apply(ParDo.of(new printMsgToConsole("[clean]")));

    // 4. Let's obtain popular apps first.
    // First, convert StructuredData to KV<AppId, DeviceId>, apply Distinct (so we can count distinct users), and apply
    // Count.perKey. Here, popular apps are the ones with 3 or more unique users.
    PCollection<String> popularApps = cleanData
        .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
            .via((StructuredData userApp) -> KV.of(userApp.getAppId(), userApp.getDeviceId())))
        .apply(Distinct.create()).apply(Count.perKey())
        .apply(Filter.by((ProcessFunction<KV<String, Long>, Boolean>) input -> input.getValue() > 2L))
        .apply(Keys.create());
    // The correct result would be ESPN and Piazza (but not Weather, which only has two unique users).
    // Recall that we removed duplicates by applying "Distcint" above.
    popularApps.apply(ParDo.of(new printStrToConsole("[popularApps]")));

    // 5. Let's turn $popularApps into a PCollectionView so that the subsequent DoFn can access it.
    PCollectionView<List<String>> popularAppsView = popularApps.apply(View.asList());

    // 6. Let's find users with 2 or more popular apps.
    // First, we need to aggregate data at user level (so each user's apps are grouped together).
    // You will get 4 lines of output: haden-iPhone has 1 popular app but others have 2 popular apps each.
    cleanData
        .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
            .via((StructuredData msg) -> KV.of(msg.getDeviceId(), msg.getAppId())))
        .apply(GroupByKey.create()).apply(ParDo.of(new DoFn<KV<String, Iterable<String>>, String>() {
          @ProcessElement
          public void process(@Element KV<String, Iterable<String>> elem, OutputReceiver<String> out,
              ProcessContext c) {
            // Note: elem.getKey() corresponds to "DeviceId" and elem.getValue() corresponds to an iterable of AppIds
            // that this user has.
            int cntPopularApps = 0;
            for (String app : elem.getValue()) {
              // NOTE: This may not be efficient, if your "List" is large. How can we make this more efficient?
              if (c.sideInput(popularAppsView).contains(app)) {
                cntPopularApps++;
              }
            }
            // For this toy example pipeline, let's print out these values for sanity check.
            System.out.format("[list view] User (%s) has (%d) popular app(s)\n", elem.getKey(), cntPopularApps);
            if (cntPopularApps >= 2) {
              out.output(elem.getKey());
            }
          }
        }).withSideInputs(popularAppsView));

    // 7. TODO: Homework 1: Re-do steps 5 & 6 above using View.asSingleton() (instead of List).
    // Feel free to share your code for this homework on Piazza (it won't be graded, so you can share).
    {
      // PCollectionView<?> singletonView = popularApps.<apply some PTransforms>.apply(View.asSingleton());
      cleanData
          .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
              .via((StructuredData msg) -> KV.of(msg.getDeviceId(), msg.getAppId())))
          .apply(GroupByKey.create()).apply(ParDo.of(new DoFn<KV<String, Iterable<String>>, String>() {
            @ProcessElement
            public void process(@Element KV<String, Iterable<String>> elem, OutputReceiver<String> out,
                ProcessContext c) {
              int cntPopularApps = 0;
              for (String app : elem.getValue()) {
                // TODO: Do this with "singletonView".
              }
              // For this toy example pipeline, let's print out these values for sanity check.
              // System.out.format("[singleton view] User (%s) has (%d) popular app(s)\n", elem.getKey(),
              // cntPopularApps);
              if (cntPopularApps >= 2) {
                out.output(elem.getKey());
              }
            }
          })); // TODO: Don't forget to specify ".withSideInputs(singletonView)" when you're ready.
    }

    // 8. TODO: Homework 2: Re-do steps 5 & 6 above using View.asMap() (instead of List).
    // Feel free to share your code for this homework on Piazza (it won't be graded, so you can share).
    {
      // PCollectionView<Map<String, Boolean>> mapView = popularApps.<apply some PTransforms>.apply(View.asMap());
      cleanData
          .apply(MapElements.into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
              .via((StructuredData msg) -> KV.of(msg.getDeviceId(), msg.getAppId())))
          .apply(GroupByKey.create()).apply(ParDo.of(new DoFn<KV<String, Iterable<String>>, String>() {
            @ProcessElement
            public void process(@Element KV<String, Iterable<String>> elem, OutputReceiver<String> out,
                ProcessContext c) {
              int cntPopularApps = 0;
              for (String app : elem.getValue()) {
                // TODO: Do this with "mapView".
              }
              // For this toy example pipeline, let's print out these values for sanity check.
              // System.out.format("[map view] User (%s) has (%d) popular app(s)\n", elem.getKey(), cntPopularApps);
              if (cntPopularApps >= 2) {
                out.output(elem.getKey());
              }
            }
          })); // TODO: Don't forget to specify ".withSideInputs(mapView)" when you're ready.
    }

    // Now, run it!
    p.run().waitUntilFinish();

    System.out.println("\nta-da! done!");
  }

  static class printStrToConsole extends DoFn<String, Void> {
    final String prefix;

    public printStrToConsole(String prefix) {
      this.prefix = prefix;
    }

    @ProcessElement
    public void process(@Element String elem) {
      System.out.format("[%s] (%s)\n", prefix, elem);
    }
  }

  static class printMsgToConsole extends DoFn<Message, Void> {
    final String prefix;

    public printMsgToConsole(String prefix) {
      this.prefix = prefix;
    }

    @ProcessElement
    public void process(@Element Message elem) throws InvalidProtocolBufferException {
      System.out.format("[%s] %s\n", prefix, ProtoUtils.getJsonFromMessage(elem));
    }
  }
}
