package org.mbari.mxm.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderApiType;

@QuarkusTest
public class ProviderResourceTest extends BaseForTests {

  private void post(Provider pl) {
    given()
        .contentType("application/json")
        .body(pl)
        .when()
        .post("/providers")
        .then()
        .statusCode(201);
  }

  @Test
  public void testSomeRequests() {
    // insertAssetClass some:
    var providerIds = Arrays.asList("Q1", "Q2");

    for (String providerId : providerIds) {
      post(
          new Provider(
              providerId,
              String.format("https://%s.net", providerId),
              ProviderApiType.REST,
              "originalDescription",
              null,
              null,
              null,
              null,
              null));
    }

    var r1 = given().when().get("/providers").then().statusCode(200);

    for (String name : providerIds) {
      r1.body(containsString(name));
    }

    for (String providerId : providerIds) {
      given().when().delete("/providers/" + providerId).then().statusCode(200);

      given().when().get("/providers/" + providerId).then().statusCode(404);
    }
  }
}
