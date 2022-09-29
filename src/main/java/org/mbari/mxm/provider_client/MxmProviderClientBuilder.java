package org.mbari.mxm.provider_client;

import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.provider_client.rest.MxmProviderClientRest;

public final class MxmProviderClientBuilder {

  /**
   * Creates client to interact with external provider.
   *
   * @param providerId Provider ID.
   * @param httpEndpoint Base URL for requests to provider.
   * @param apiType NOTE: Only the REST Provider API is supported at the moment.
   */
  public static MxmProviderClient create(
      String providerId, String httpEndpoint, ProviderApiType apiType) {
    if (apiType != ProviderApiType.REST) {
      throw new IllegalArgumentException("Only the REST Provider API is supported.");
    }
    return new MxmProviderClientRest(providerId, httpEndpoint);
  }

  private MxmProviderClientBuilder() {}
}
