package org.mbari.mxm.graphql.input;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.mbari.mxm.db.provider.ProviderApiType;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderCreate {

  @NotNull public String providerId;
  @NotNull public String httpEndpoint;
  @NotNull public ProviderApiType apiType;
}
