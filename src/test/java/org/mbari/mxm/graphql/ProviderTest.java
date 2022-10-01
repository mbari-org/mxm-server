package org.mbari.mxm.graphql;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Collections;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.PostgresResource;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.db.unit.Unit;
import org.mbari.mxm.provider_client.WireMockProviderRest;

@QuarkusTest
@QuarkusTestResource(
    value = WireMockProviderRest.class,
    initArgs = {
      @ResourceArg(name = "port", value = "8890"),
    })
@QuarkusTestResource(PostgresResource.class)
@Slf4j
public class ProviderTest extends BaseForTests {

  private Provider createProvider(Provider pl) throws JsonProcessingException {
    String requestBody =
        bodyForRequest(
            """
            mutation($pl: ProviderInput!) {
              createProvider(pl: $pl) {
                providerId
                apiType
                httpEndpoint
                description
              }
            }
        """,
            Collections.singletonMap("pl", pl));

    return given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        .statusCode(200)
        .extract()
        .body()
        .jsonPath()
        .getObject("data.createProvider", Provider.class);
  }

  private Provider getProvider(String providerId) throws JsonProcessingException {
    String requestBody =
        bodyForRequest(
            """
        query ($providerId: String!) {
          provider(providerId: $providerId) {
            providerId
            apiType
            httpEndpoint
            description
            descriptionFormat
            usesSched
            canValidate
            usesUnits
            canReportMissionStatus
          }
        }
        """,
            Collections.singletonMap("providerId", providerId));

    return given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        .statusCode(200)
        .extract()
        .body()
        .jsonPath()
        .getObject("data.provider", Provider.class);
  }

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

  private Unit getUnit(String providerId, String unitName) throws JsonProcessingException {
    var variables = new HashMap<String, Object>();
    variables.put("providerId", providerId);
    variables.put("unitName", unitName);
    String requestBody =
        bodyForRequest(
            """
        query unit($providerId: String!, $unitName: String!) {
          unit(providerId: $providerId, unitName: $unitName) {
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

  private Provider updateProvider(Provider pl) throws JsonProcessingException {
    log.debug("updateProvider: pl={}", objectWriter.writeValueAsString(pl));
    String requestBody =
        bodyForRequest(
            """
          mutation ($pl: ProviderInput!) {
            updateProvider(pl: $pl) {
              providerId
              description
              descriptionFormat
              canValidate
            }
          }
        """,
            Collections.singletonMap("pl", pl));
    log.debug("updateProvider: requestBody={}\n", requestBody);

    var res =
        given()
            .body(requestBody)
            .post("/graphql/")
            .then()
            .contentType(ContentType.JSON)
            .statusCode(200)
            .extract();

    log.debug("updateProvider: res={}\n", res.asString());
    return res.body().jsonPath().getObject("data.updateProvider", Provider.class);
  }

  private Provider deleteProvider(Provider pl) throws JsonProcessingException {
    String requestBody =
        bodyForRequest(
            """
        mutation ($pl: ProviderInput!) {
          deleteProvider(pl: $pl) {
            providerId
            apiType
            httpEndpoint
            description
            descriptionFormat
            usesSched
            canValidate
            usesUnits
            canReportMissionStatus
          }
        }
        """,
            Collections.singletonMap("pl", pl));

    return given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        .statusCode(200)
        .extract()
        .body()
        .jsonPath()
        .getObject("data.deleteProvider", Provider.class);
  }

  @Test
  public void crud() throws JsonProcessingException {

    // verify base entities (for now created upon database set-up):
    final var ac = getAssetClass("LRAUV");
    assertEquals(
        "Long-Range Autonomous Underwater Vehicle".toLowerCase(), ac.description.toLowerCase());
    final var sim = getAsset("sim");
    assertEquals("sim", sim.assetId);
    assertEquals("LRAUV", sim.className);

    final var providerId = "ProviderTest";

    final var deletePayload = new Provider(providerId);

    // not there yet:
    assertNull(deleteProvider(deletePayload));

    // create:
    final var newPl = new Provider(providerId);
    newPl.setHttpEndpoint("http://localhost:8890/api");
    newPl.setApiType(ProviderApiType.REST);

    final var created = createProvider(newPl);
    assertEquals(newPl.providerId, created.providerId);
    assertEquals(newPl.apiType, created.apiType);
    assertEquals(newPl.httpEndpoint, created.httpEndpoint);
    // given by the mock provider
    assertEquals("TethysDash/LRAUV System", created.description);

    // get:
    final var retrieved = getProvider(providerId);
    // explicitly given in creation:
    assertEquals(newPl.providerId, retrieved.providerId);
    assertEquals(newPl.apiType, retrieved.apiType);
    assertEquals(newPl.httpEndpoint, retrieved.httpEndpoint);

    // given my provider itself (via mock):
    assertEquals("TethysDash/LRAUV System", retrieved.description);
    assertTrue(retrieved.usesSched);
    assertFalse(retrieved.canValidate);
    assertTrue(retrieved.usesUnits);
    assertFalse(retrieved.canReportMissionStatus);

    // update:
    final var upl = new Provider(providerId);
    upl.description = "updated description";
    upl.descriptionFormat = "updated descriptionFormat";
    upl.canValidate = true;
    final var updated = updateProvider(upl);
    assertEquals(newPl.providerId, updated.providerId);
    assertEquals(upl.description, updated.description);
    assertEquals(upl.descriptionFormat, updated.descriptionFormat);
    assertTrue(updated.canValidate);

    // verify generated entities upon the addition of the provider:
    final var Ah = getUnit(providerId, "ampere_hour");
    assertEquals("ampere_hour", Ah.unitName);
    assertEquals("Ah", Ah.abbreviation);
    assertEquals("ampere_second", Ah.baseUnit);
    // TODO other entities

    // delete:
    final var deleted = deleteProvider(deletePayload);
    assertEquals(deleted.providerId, retrieved.providerId);

    // some more checks:
    assertNull(getProvider(providerId));
    assertNull(updateProvider(upl));
    assertNull(deleteProvider(deletePayload));
  }
}
