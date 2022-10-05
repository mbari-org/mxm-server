package org.mbari.mxm.graphql.exc;

public abstract class MxmException extends RuntimeException {

  public MxmException(String message, Throwable cause) {
    super("MxmException:" + message, cause);
  }

  public MxmException(String message) {
    super("MxmException:" + message);
  }
}
