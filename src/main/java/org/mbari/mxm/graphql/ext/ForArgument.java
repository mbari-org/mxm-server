package org.mbari.mxm.graphql.ext;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
