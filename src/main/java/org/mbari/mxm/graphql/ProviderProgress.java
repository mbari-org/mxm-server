package org.mbari.mxm.graphql;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderProgress {

  @NotNull public String providerId;
  public String message;
  public Double percentComplete;
}
