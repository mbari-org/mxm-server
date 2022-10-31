package org.mbari.mxm.graphql;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.PostgresResource;
import org.mbari.mxm.db.unit.Unit;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@Slf4j
public class UnitTest extends BaseForTests {
  private Unit getUnit(String unitName) throws JsonProcessingException {
    var variables = new HashMap<String, Object>();
    variables.put("unitName", unitName);
    String requestBody =
        bodyForRequest(
            """
            query unit($unitName: String!) {
              unit(unitName: $unitName) {
                unitName
                abbreviation
                baseUnit
              }
            }
            """,
            variables);

    return given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        .statusCode(200)
        .extract()
        .body()
        .jsonPath()
        .getObject("data.unit", Unit.class);
  }

  @Test
  public void units() throws JsonProcessingException {
    final var cm = getUnit("centimeter");
    assertEquals("centimeter", cm.unitName);
    assertEquals("cm", cm.abbreviation);
    assertEquals("meter", cm.baseUnit);

    final var degree = getUnit("degree");
    assertEquals("degree", degree.unitName);
    assertEquals("arcdeg", degree.abbreviation);
    assertEquals("radian", degree.baseUnit);

    final var Ah = getUnit("ampere_hour");
    assertEquals("ampere_hour", Ah.unitName);
    assertEquals("Ah", Ah.abbreviation);
    assertEquals("ampere_second", Ah.baseUnit);
  }

  private List<Unit> getDerivedUnits(String unitName) throws JsonProcessingException {
    var variables = new HashMap<String, Object>();
    variables.put("unitName", unitName);
    String requestBody =
        bodyForRequest(
            """
              query derivedUnits($unitName: String!) {
                  unit(unitName: $unitName) {
                    derivedUnits {
                      unitName
                      abbreviation
                      baseUnit
                    }
                  }
                }
              """,
            variables);

    return given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        .statusCode(200)
        .extract()
        .body()
        .jsonPath()
        .getList("data.unit.derivedUnits", Unit.class);
  }

  @Test
  public void derivedUnits() throws JsonProcessingException {
    final var meterDerived = getDerivedUnits("meter");
    assertThat(meterDerived).hasSize(7);

    final var radianDerived = getDerivedUnits("radian");
    // actually 4, but we added 'psu' to the database
    assertThat(radianDerived).hasSize(5);
  }
}
