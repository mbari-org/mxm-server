package org.mbari.mxm.graphql;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.PostgresResource;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.graphql.input.ProviderCreate;
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
  private Provider createProvider(ProviderCreate pl) throws JsonProcessingException {
    String requestBody =
        bodyForRequest(
            """
            mutation($pl: ProviderCreateInput!) {
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
    final var providerId = "ProviderTest";

    final var deletePayload = new Provider(providerId);

    // not there yet:
    assertNull(deleteProvider(deletePayload));

    // create:
    final var newPl =
        ProviderCreate.builder()
            .providerId(providerId)
            .httpEndpoint("http://localhost:8890/api")
            .apiType(ProviderApiType.REST)
            .build();
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

    // delete:
    final var deleted = deleteProvider(deletePayload);
    assertEquals(deleted.providerId, retrieved.providerId);

    // some more checks:
    assertNull(getProvider(providerId));
    assertNull(updateProvider(upl));
    assertNull(deleteProvider(deletePayload));
  }
}
