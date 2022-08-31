package org.mbari.mxm.provider_client.responses;

import java.util.List;
import lombok.Data;

@Data
public class UnitsResponse {

  public List<Unit> result;

  @Data
  public static class Unit {
    public String name;
    public String abbreviation;
    public String baseUnit;
  }
}
