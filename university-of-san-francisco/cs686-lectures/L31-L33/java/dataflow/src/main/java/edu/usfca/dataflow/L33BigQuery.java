package edu.usfca.dataflow;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.runners.direct.DirectRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.SchemaAndRecord;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.common.base.Objects;

public class L33BigQuery {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  // TODO: Make sure you change the following parameters according to your own settings.
  final static String GCP_PROJECT_ID = "beer-spear";
  final static String GCS_BUCKET = "gs://usf-cs686-sp20";
  final static String REGION = "us-west1";
  final static String MY_BQ_DATASET = "cs686"; // <- change this to an existing BigQuery dataset in your project.
  final static String MY_BQ_TABLE = "L32_table"; // <- this can be anything. if it does not exist, it'll be created.
  final static TableReference destTable =
      new TableReference().setProjectId(GCP_PROJECT_ID).setDatasetId(MY_BQ_DATASET).setTableId(MY_BQ_TABLE);

  static DataflowPipelineOptions getOptions() {
    String jobName = String.format("L32-df-bq-%06d", Instant.now().getMillis() % 1000000);

    DataflowPipelineOptions options = PipelineOptionsFactory.create().as(DataflowPipelineOptions.class);

    // This will display the "current settings" stored in options.
    System.out.println(options.toString());

    options.setTempLocation(GCS_BUCKET + "/staging");
    options.setJobName(jobName);
    // options.setRunner(DataflowRunner.class); // <- Runner on GCP
    // TODO: Try with the local runner first, and when things work, try it on GCP.
    // The reason being, it takes a few minutes to run it on GCP.
    options.setRunner(DirectRunner.class); // <- Local Runner
    options.setMaxNumWorkers(1);
    options.setWorkerMachineType("n1-standard-1");
    options.setDiskSizeGb(150);
    options.setRegion(REGION);
    options.setProject(GCP_PROJECT_ID);

    // You will see more info here.
    // To run a pipeline (job) on GCP via Dataflow, you need to specify a few things like the ones above.
    System.out.println(options.toString());
    return options;
  }

  public static void execute(String[] args) throws IOException {

    DataflowPipelineOptions options = getOptions();

    Pipeline p = Pipeline.create(options);

    System.out.println(System.getProperty("user.dir"));

    final String query = String.join("\n",
        Files.readAllLines(Paths.get("dataflow/resources/lab5-taskA-join.sql"), Charset.forName("UTF-8")));

    System.out.println("QUERY: \n" + query);

    // Read from BigQuery.
    PCollection<MyData> data = p.apply(BigQueryIO.read(new SerializableFunction<SchemaAndRecord, MyData>() {
      @Override
      public MyData apply(SchemaAndRecord row) {
        // Note: GenericRecord is defined in org.apache.avro.
        final GenericRecord record = row.getRecord();

        // TODO: The following lines will lead to problems. Fix them all!
        // hint: Check (Dataflow) Console logs (on the web or on your console), and Search on Google/StackOverflow!
        final String name = (String) record.get("name");
        final int year = (Integer) record.get("year");
        final int total = (Integer) record.get("total_newborns");
        final int ranking = (Integer) record.get("ranking");

        return new MyData(name, year, total, ranking);
      }
    }).fromQuery(query).usingStandardSql());

    data = data.apply(Filter.by((SerializableFunction<MyData, Boolean>) input -> {
      // Filtering, for the sake of demonstration.
      final boolean condition = input.ranking == 1 && input.year >= 2000 && input.year <= 2010;
      if (condition) {
        // Be sure to check the Dataflow Web Console page's "logs" in the "Filter" step.
        // You should see 11 logs.
        LOG.info("[MyData] {}", input.toString());
      }
      return condition;
    }));

    // Write to BigQuery
    List<TableFieldSchema> fields = new ArrayList<>();
    fields.add(new TableFieldSchema().setName("name").setType("STRING"));
    // TODO Uncomment and complete the implementation!
    // fields.add(new TableFieldSchema().setName("total")....);
    // fields.add(new TableFieldSchema().setName("year")....);

    data.apply(BigQueryIO.<MyData>write().to(destTable)//
        .withWriteDisposition(WriteDisposition.WRITE_TRUNCATE)// <- TODO what other options are available?
        .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED) // <- TODO what other options are available?
        .withSchema(new TableSchema().setFields(fields))//
        .withFormatFunction((SerializableFunction<MyData, TableRow>) input//
    -> new TableRow().set("name", input.name).set("year", input.year)//
        .set("total", input.totalNewborns)));

    // Once the job successfully finishes, then visit your BigQuery website and check if the table was created by this
    // job. Also, check the contents of the table. It should have 11 rows and 3 columns.
    p.run().waitUntilFinish();
  }

  // This could have been a proto message, but for the sake of demonstration, here's a Java class.
  static class MyData implements Serializable {
    final String name;
    final Integer year, totalNewborns, ranking;

    public MyData(String name, int year, int totalNewborns, int ranking) {
      this.name = name;
      this.year = year;
      this.totalNewborns = totalNewborns;
      this.ranking = ranking;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      MyData myData = (MyData) o;
      return year == myData.year && totalNewborns == myData.totalNewborns && ranking == myData.ranking
          && Objects.equal(name, myData.name);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, year, totalNewborns, ranking);
    }

    @Override
    public String toString() {
      return String.format("name: %s year: %4d total: %6d ranking: %d", name, year, totalNewborns, ranking);
    }
  }
}
