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
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.assetClass.AssetClass;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@Slf4j
public class AssetTest extends BaseForTests {
  private AssetClass getAssetClass(String className) throws JsonProcessingException {
    var variables = new HashMap<String, Object>();
    variables.put("className", className);
    String requestBody =
        bodyForRequest(
            """
              query assetClass($className: String!) {
                assetClass(className: $className) {
                  description
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
        .getObject("data.assetClass", AssetClass.class);
  }

  @Test
  public void assetClasses() throws JsonProcessingException {
    final var ac = getAssetClass("LRAUV");
    assertEquals(
        "Long-Range Autonomous Underwater Vehicle".toLowerCase(), ac.description.toLowerCase());
  }

  private Asset getAsset(String assetId) throws JsonProcessingException {
    var variables = new HashMap<String, Object>();
    variables.put("assetId", assetId);
    String requestBody =
        bodyForRequest(
            """
              query asset($assetId: String!) {
                asset(assetId: $assetId) {
                  assetId
                  className
                  description
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
        .getObject("data.asset", Asset.class);
  }

  @Test
  public void assets() throws JsonProcessingException {
    final var sim = getAsset("sim");
    assertEquals("sim", sim.assetId);
    assertEquals("LRAUV", sim.className);

    final var tiny = getAsset("Tiny");
    assertEquals("Tiny", tiny.assetId);
    assertEquals("Waveglider", tiny.className);
  }

  private List<Asset> getAssetClassInstance(String className) throws JsonProcessingException {
    var variables = new HashMap<String, Object>();
    variables.put("className", className);
    String requestBody =
        bodyForRequest(
            """
              query assetClass($className: String!) {
                assetClass(className: $className) {
                  assets {
                    assetId
                    description
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
        .getList("data.assetClass.assets", Asset.class);
  }

  @Test
  public void assetClassInstances() throws JsonProcessingException {
    assertThat(getAssetClassInstance("LRAUV")).hasSize(4);
    assertThat(getAssetClassInstance("Waveglider")).hasSize(4);
    assertThat(getAssetClassInstance("AcmeDevice")).hasSize(3);
    assertThat(getAssetClassInstance("FooPlat")).hasSize(2);
  }
}
