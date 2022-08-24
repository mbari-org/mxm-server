package org.mbari.mxm.provider_client.responses;

import lombok.Data;

import java.util.List;

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
