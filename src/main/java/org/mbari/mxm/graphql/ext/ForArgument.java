package org.mbari.mxm.graphql.ext;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.argument.Argument;
import org.mbari.mxm.db.parameter.Parameter;
import org.mbari.mxm.db.parameter.ParameterService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForArgument {
  @Inject ParameterService parameterService;

  public List<Parameter> parameter(@Source List<Argument> arguments) {
    return parameterService.getParametersMultipleArguments(arguments);
  }
}
