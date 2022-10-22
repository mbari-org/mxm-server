package org.mbari.mxm.graphql.ext;

import static java.util.stream.Collectors.toList;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.parameter.Parameter;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForParameter {
  @Inject ProviderService providerService;

  public List<Provider> provider(@Source List<Parameter> parameters) {
    List<String> providerIds = parameters.stream().map(e -> e.providerId).collect(toList());
    return providerService.getProviders(providerIds);
  }
}
