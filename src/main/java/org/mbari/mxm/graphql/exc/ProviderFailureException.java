package org.mbari.mxm.graphql.exc;

import io.smallrye.graphql.api.ErrorCode;

@ErrorCode("Problem while communicating with provider")
public class ProviderFailureException extends MxmException {

  public ProviderFailureException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProviderFailureException(String message) {
    super(message);
  }
}
