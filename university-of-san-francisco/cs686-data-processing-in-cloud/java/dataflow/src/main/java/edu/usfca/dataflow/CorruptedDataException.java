package edu.usfca.dataflow;

// This is a wrapper class used mainly for grading.
// This can be useful for you when you are debugging your code (because you can include arbitrary messages in
// exception).
public class CorruptedDataException extends IllegalArgumentException {

  public CorruptedDataException() {
    super();
  }

  public CorruptedDataException(String s) {
    super(s);
  }

  public CorruptedDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public CorruptedDataException(Throwable cause) {
    super(cause);
  }

  private static final long serialVersionUID = -686L;
}
