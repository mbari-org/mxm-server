package org.mbari.mxm.graphql.exc;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("DB violation while interacting with provider")
public class DbViolationException extends MxmException {

  public DbViolationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DbViolationException(String message) {
    super(message);
  }
}
