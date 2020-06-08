package edu.usfca.dataflow.utils;

import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.TypeDescriptors;

import com.google.protobuf.Message;

import edu.usfca.dataflow.MyOptions;

public class IOUtils {
  // This is provided for your convenience as you'll need to use it multiple times.
  // You do NOT have to use this, but it'll come handy.

  public static <T extends Message> PDone encodeB64AndWrite(PCollection<T> pc, String outputPath) {
    return pc.apply(MapElements.into(TypeDescriptors.strings()).via((T elem) -> ProtoUtils.encodeMessageBase64(elem)))
        .apply(TextIO.write().to(outputPath).withSuffix(".txt")
            .withNumShards(pc.getPipeline().getOptions().as(MyOptions.class).getIsLocal() ? 1 : 0));
  }
}
