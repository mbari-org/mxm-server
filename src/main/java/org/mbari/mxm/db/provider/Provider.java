package org.mbari.mxm.db.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Provider  {

  @NotNull
  public String providerId;

  public Provider(String providerId) {
    this.providerId = providerId;
  }

  public String httpEndpoint;
  public ProviderApiType apiType;
  public String description;
  public String descriptionFormat;
  public Boolean usesSched;
  public Boolean canValidate;
  public Boolean usesUnits;
  public Boolean canReportMissionStatus;
}
