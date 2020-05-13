package edu.usfca.dataflow;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;

import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableScanConfiguration;
import com.google.cloud.bigtable.beam.CloudBigtableScanConfiguration.Builder;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;

/**
 * NOTE: This code is not runnable out-of-the-box, and this is completely optional.
 *
 * You'll need to create and configure a Bigtable instance (which you should only do after you complete project 5).
 *
 * Bigtable instances are very expensive, and you may accidentally use up all of your GCP credits.
 *
 * This sample code is intended as an illustration of how one can read from / write to Bigtable using Beam SDK &
 * Bigtable Connector. If you want to run this, you'll first need to write (dummy) data to your Bigtable instance.
 *
 * To do so, first follow the documentation of Cloud Bigtable, and use "cbt" (command-line cli tool) to write to
 * Bigtable.
 */
public class L35Bigtable {

  final static String MY_PROJECT_ID = "my-gcp-project"; // change this.
  final static String MY_CLUSTER_ID = "my-bt-cluster"; // change this.
  final static String MY_TABLE_ID = "my-bt-table"; // change this.

  public static void run() {
    Scan scan = new Scan();

    scan.setCacheBlocks(false).setMaxVersions(1); // Disable caching and read only up to three latest cells.
    scan.addFamily(Bytes.toBytes("cf")); // Read only a specific column family (cf).
    // Scan a subset of rows, using prefix (row keys are sorted!).
    // (These are deprecated, but it's provided as illustration; new methods can be used for a range scan.)
    scan.setStartRow(Bytes.toBytes("id:abc"));
    scan.setStopRow(Bytes.toBytes("id:xyz"));
    try {
      // Effectively, scan the cells that were modified within this window.
      long tsBegin = DateTime.parse("2020-05-02T00:00:00Z").minusDays(1).getMillis();
      long tsEnd = DateTime.parse("2020-05-02T00:00:00Z").getMillis();
      scan.setTimeRange(tsBegin, tsEnd);
    } catch (IOException e) {
      return;
    }

    CloudBigtableTableConfiguration.Builder readConfig = new CloudBigtableScanConfiguration.Builder()
        .withProjectId(MY_PROJECT_ID).withInstanceId(MY_CLUSTER_ID).withTableId(MY_TABLE_ID).withScan(scan);

    // TODO: By following previous labs/projects, create "Options" object and set the default values appropriately.
    Pipeline pipeline = Pipeline.create();

    PCollection<Mutation> mutations = pipeline.apply(Read.from(CloudBigtableIO.read(((Builder) readConfig).build())))
        .apply(ParDo.of(new Converter()));

    CloudBigtableTableConfiguration writeConfig = new CloudBigtableScanConfiguration.Builder()
        .withProjectId(MY_PROJECT_ID).withInstanceId(MY_CLUSTER_ID).withTableId(MY_TABLE_ID).build();

    if (writeConfig != null) {
      mutations.apply(CloudBigtableIO.writeToTable(writeConfig));
    }

    pipeline.run().waitUntilFinish();
  }

  /**
   * Given a Result object (which contains all cells coming from the same row) based on the "Scan" configuration
   * specified earlier,
   *
   * this DoFn produces "Delete" mutations and "Put" mutations (the logic is arbitrary, because this is just an
   * illustration), which will be written back to your Bigtable.
   *
   * Note that both "Delete" and "Put" are mutations.
   */
  static class Converter extends DoFn<Result, Mutation> {

    @ProcessElement
    public void processElement(ProcessContext c) {
      Result result = c.element();
      final byte[] rowKey = result.getRow();

      boolean shouldOutput = false;
      Delete outputMutation = new Delete(rowKey);

      for (Entry<byte[], byte[]> entry : result.getFamilyMap(Bytes.toBytes("cf")).entrySet()) {
        final byte[] colQualifier = entry.getKey();
        final byte[] value = entry.getValue();

        if (value != null && value.length % 4 == 0) {
          outputMutation.addColumns(Bytes.toBytes("cf"), colQualifier);
          shouldOutput = true;
        } else if (value != null && value.length % 4 == 2) {
          // Copy the same data to another column family (cf2).
          c.output(new Put(rowKey).addImmutable(Bytes.toBytes("cf2"), colQualifier, value));
        }
      }

      if (shouldOutput) {
        c.output(outputMutation);
      }
    }
  }
}
