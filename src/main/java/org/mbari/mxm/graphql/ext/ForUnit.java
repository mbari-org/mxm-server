package org.mbari.mxm.graphql.ext;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.unit.Unit;
import org.mbari.mxm.db.unit.UnitService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForUnit {
  @Inject UnitService unitService;

  public List<List<Unit>> derivedUnits(@Source List<Unit> units) {
    return unitService.getDerivedUnitsMultiple(units);
  }
}
