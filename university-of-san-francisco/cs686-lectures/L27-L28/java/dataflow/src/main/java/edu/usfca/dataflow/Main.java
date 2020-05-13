package edu.usfca.dataflow;

import java.io.Serializable;

import com.google.common.base.Objects;

public class Main {
  public static class MyData implements Serializable {
    final private Long eventAt;
    final private String key;

    private MyData(String key, Long eventAt) {
      this.key = key;
      this.eventAt = eventAt;
    }

    static MyData of(String key, Long eventAt) {
      return new MyData(key, eventAt);
    }

    // The following "WARNING" messages may appear if you do not override equals() properly:
    // WARNING: Coder of type class org.apache.beam.sdk.coders.SerializableCoder has a #structuralValue method which
    // does not return true when the encoding of the elements is equal. Element
    // edu.usfca.dataflow.TestWindows$MyData@262b1ea9
    //

    // auto-generated via Guava.
    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      MyData myData = (MyData) o;
      return Objects.equal(eventAt, myData.eventAt) && Objects.equal(key, myData.key);
    }

    // auto-generated via Guava.
    @Override
    public int hashCode() {
      return Objects.hashCode(eventAt, key);
    }
  }

  public static void main(String[] args) {

  }
}
