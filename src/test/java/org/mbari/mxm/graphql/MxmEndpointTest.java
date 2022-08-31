package org.mbari.mxm.graphql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.PostgresResource;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
@Slf4j
public class MxmEndpointTest extends BaseForTests {

  @Test
  public void allProviders() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        {
           allProviders {
             apiType
             description
             descriptionFormat
             httpEndpoint
             providerId
             assetClasses {
               className
               description
               assets {
                 assetId
                 description
               }
             }
             assets {
               assetId
               description
             }
           }
         }
         """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allProviders.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  @Test
  public void getProvider() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        query($providerId: String!) {
          provider(providerId: $providerId) {
            canValidate
            httpEndpoint
            apiType
            assetClasses {
              className
              description
              assets {
                assetId
                description
              }
            }
            assets {
              assetId
              description
            }
          }
        }
        """,
            Collections.singletonMap("providerId", "NEW_PROV"));

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        .statusCode(200);
  }

  ///////////

  @Test
  public void allAssetClasses() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        {
          allAssetClasses {
            providerId
            className
          }
        }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allAssetClasses.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  ///////////

  @Test
  public void allAssets() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        {
           allAssets {
             providerId
             assetId
             className
           }
         }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allAssets.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  ///////////

  @Test
  public void allMissionTemplates() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        {
            allMissionTemplates {
              providerId
              missionTplId
              description
            }
          }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allMissionTemplates.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  ///////////

  @Test
  public void allMissionTemplateAssetClasses() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        {
          allMissionTemplateAssetClasses {
            providerId
            missionTplId
            assetClassName
          }
        }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allMissionTemplateAssetClasses.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  ///////////

  @Test
  public void allUnits() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
        {
            allUnits {
              providerId
              unitName
            }
          }
         """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allUnits.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  ///////////

  @Test
  public void allParameters() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
         {
          allParameters {
            paramName
          }
        }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allParameters.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  ///////////

  @Test
  public void allMissions() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
         {
            allMissions {
              providerId
              missionTplId
              missionId
            }
          }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allMissions.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }

  @Test
  public void allMissionsWithArguments() throws JsonProcessingException {

    String requestBody =
        bodyForRequest(
            """
         {
           allMissions {
             providerId
             missionTplId
             missionId
             arguments {
               paramName
               paramValue
               paramUnits
             }
           }
         }
        """);

    given()
        .body(requestBody)
        .post("/graphql/")
        .then()
        .contentType(ContentType.JSON)
        // >= 0 just for initial testing:
        .body("data.allMissions.size()", greaterThanOrEqualTo(0))
        .statusCode(200);
  }
}
