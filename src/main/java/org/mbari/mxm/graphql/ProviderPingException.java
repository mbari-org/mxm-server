package org.mbari.mxm.graphql;

import org.eclipse.microprofile.graphql.GraphQLException;

public class ProviderPingException extends GraphQLException {
  public ProviderPingException(String providerId, String httpEndpoint, Throwable cause) {
    super("Error pinging provider '" + providerId + "'" +
      ", base http endpoint: '" + httpEndpoint + "'", cause);
  }

}
