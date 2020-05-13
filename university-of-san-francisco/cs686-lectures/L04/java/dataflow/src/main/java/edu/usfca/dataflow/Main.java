package edu.usfca.dataflow;

import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Distinct;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.Keys;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.ProcessFunction;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.lang3.StringUtils;

import edu.usfca.protobuf.Common.StructuredData;

public class Main {

  // Code used for lecture 04.
  public static void main(String[] args) {

    runSamplePipeline1();

    // runSamplePipeline2();
  }

  static void runSamplePipeline1() {

    // 1. Create a pipeline object and set things up.
    Pipeline p = Pipeline.create();
    // This tells Beam to use local machine (not Google Dataflow Runner).
    p.getOptions().setRunner(DirectRunner.class);

    // 2. Suppose we're reading CSV (text) data from some data source.
    // For the sake of simplicity, we're creating dummy data from an in-memory Java collection.
    // Let's also assume that there's no "duplicate" data points.
    PCollection<String> inputData = p.apply(Create.of(//
        "haden-iPhone,ESPN", "haden-iPhone,Weather", //
        //
        "android-1234,ESPN", "android-1234,Canvas", "android-1234,Piazza", //
        //
        "android-686,ESPN", "android-686,Piazza", //
        //
        "iPhone-LS307,ESPN", "iPhone-LS307,Piazza", "iPhone-LS307,Weather", //
        //
        "some corrupted data", ""//
    ));

    // 3. First, we filter out corrupted data.
    PCollection<String> filteredData = inputData.apply( //
        Filter.by(new ProcessFunction<String, Boolean>() {
          @Override
          public Boolean apply(String input) {
            if (StringUtils.isBlank(input)) {
              return false;
            }
            // NOTE: This is not the right way to do this, but for simplicity,
            // let's just do this.
            return input.split(",").length == 2;
          }
        }));

    // 4. Next, we transform raw data (CSV) to structured data.
    PCollection<StructuredData> userApps = filteredData.apply( //
        ParDo.of(new DoFn<String, StructuredData>() {
          @ProcessElement
          public void process(@Element String line, OutputReceiver<StructuredData> out) {
            System.out.format("Processing: '%s'\n", line);
            // NOTE: This is horrible code, and you shouldn't produce code like this.
            out.output(StructuredData.newBuilder() //
                .setDeviceId(line.split(",")[0]) //
                .setAppId(line.split(",")[1]).build());
          }
        }));

    // 5. How to count the number of (unique) users per app?
    // We need KV<T,S> so we can "aggregate" at "T" (key) level.
    userApps.apply(ParDo.of(new DoFn<StructuredData, KV<String, String>>() {
      @ProcessElement
      public void process(@Element StructuredData kv, OutputReceiver<KV<String, String>> out) {
        out.output(KV.of(kv.getAppId(), kv.getDeviceId()));
      }
    })) // <-- What'd be the type of object returned by this apply() call?
        .apply(Count.perKey()) // <-- What'd be the type of object returned by this apply() call?
        .apply(Filter.by((ProcessFunction<KV<String, Long>, Boolean>) input -> {
          return input.getValue() > 2; // Popular apps must satisfy this condition.
        })) // Notice that we can use lambda to specify simple DoFn logic.
        // 6. As we don't have a "data sink" yet, let's just print out to the console.
        .apply(ParDo.of(new DoFn<KV<String, Long>, Void>() {
          @ProcessElement
          public void process(@Element KV<String, Long> appAndCount) {
            System.out.format("Popular App '%s' with %d users\n", appAndCount.getKey(), appAndCount.getValue());
          }
        }));

    // 7. Run it!
    p.run().waitUntilFinish();

    System.out.println("\nta-da! done!");
  }

  // NOTE: We'll revisit this next week when we learn about "sideInput()".
  // Yet the following code demonstrates a few APIs/methods we learned in Lecture 04, so you should play with it!
  static void runSamplePipeline2() {

    // 1. Create a pipeline object and set things up.
    Pipeline p = Pipeline.create();
    p.getOptions().setRunner(DirectRunner.class); // This tells Beam to use local machine (not Google Dataflow Runner).

    // 2. Suppose we're reading CSV (text) data from some data source.
    // For the sake of simplicity, we're creating dummy data from an in-memory Java collection.
    PCollection<String> inputData = p.apply(Create.of(//
        "haden-iPhone,ESPN", "haden-iPhone,Weather", //
        "haden-iPhone,ESPN", "haden-iPhone,Weather", // <- intentional.
        //
        "android-1234,ESPN", "android-1234,Canvas", "android-1234,Piazza", //
        //
        "android-686,ESPN", "android-686,Piazza", //
        "android-686,ESPN", "android-686,Piazza", // <- intentional.
        "android-686,ESPN", "android-686,Piazza", // <- intentional.
        //
        "iPhone-LS307,ESPN", "iPhone-LS307,Piazza", "iPhone-LS307,Weather", //
        //
        "some corrupted data", ""//
    ));

    // 3. Let's assume that the input data may have duplicates (for whatever reason).
    // We first filter out corrupted data, and then apply "Distinct" transform.
    // Then, let's work with KV<String:user, String:app>.
    PCollection<KV<String, String>> filteredDataKv = inputData.apply(Filter.by(new ProcessFunction<String, Boolean>() {
      @Override
      public Boolean apply(String input) {
        if (StringUtils.isBlank(input)) {
          return false;
        }
        // NOTE: This is not the right way to do this, but for simplicity, let's just do this.
        return input.split(",").length == 2;
      }
    })).apply(Distinct.create()).apply(ParDo.of(new DoFn<String, KV<String, String>>() {
      @ProcessElement
      public void anyName(@Element String elem, OutputReceiver<KV<String, String>> out) {
        out.output(KV.of(elem.split(",")[0], elem.split(",")[1]));
      }
    }));

    // 4. Let's obtain popular apps as PC<String>.
    PCollection<String> popularApps = filteredDataKv.apply(Values.create()) // First, drop keys (users)
        .apply(Count.perElement()) // Then count
        .apply(Filter.by((ProcessFunction<KV<String, Long>, Boolean>) input -> input.getValue() > 2)) // Filter.
        .apply(Keys.create()); // We only keep the keys (apps).

    // For sanity check, let's print it out to the console here, using helper method.
    popularApps.apply(ParDo.of(new printToConsole()));

    // 5. Let's aggregate app IDs at user level.
    // From KV<T, S>, we would get KV<T, Iterable<S>> as a result (because each key of type "T" would have 1 or more
    // values of type "S").
    PCollection<KV<String, Iterable<String>>> users = filteredDataKv.apply(GroupByKey.create());

    // 6. Now, we want to get a collection of users with two or more popular apps... but how?
    PCollection<String> usersWithTwoPopularApps =
        users.apply(Filter.by(new ProcessFunction<KV<String, Iterable<String>>, Boolean>() {
          @Override
          public Boolean apply(KV<String, Iterable<String>> user) {
            // If this user has at least two popular apps, then we want to keep the user.
            // This should be easy as we can simply use 'popularApps' from earlier, right?
            // But how can we check whether this user's app is in $popularApps or not?
            // e.g., popularApps.contains() or anything like that wouldn't work, as such methods do not exist!

            // We will come back to this later (next week), but for now let's just hard-code this for the sake of
            // completeness.
            int cntPopularApps = 0;
            for (String app : user.getValue()) { // Let's iterate over
              // TODO: Instead of hard-coded condition here, we want to utilize "popularApps" that we computed
              // previously. Left as future work.
              if ("ESPN".equals(app) || "Piazza".equals(app)) {
                cntPopularApps++;
              }
            }
            return cntPopularApps >= 2;
          }
        })).apply(Keys.create());
    // Sanity check
    usersWithTwoPopularApps.apply(ParDo.of(new printToConsole()));

    // 7. Run it!
    p.run().waitUntilFinish();

    System.out.println("\nta-da! done!");
  }

  static class printToConsole extends DoFn<String, Void> {
    @ProcessElement
    public void process(@Element String elem) {
      System.out.format("elem: %s\n", elem);
    }
  }
}
