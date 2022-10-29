package org.mbari.mxm.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionStatusType;

@QuarkusTest
public class ProviderResourceTest extends BaseForTests {

  @Test
  public void postMissionStatus() {
    var ms = new MissionStatus();
    ms.missionTplId = "123";
    ms.missionId = "456";
    ms.providerMissionId = "789";
    ms.status = MissionStatusType.RUNNING;

    var path = String.format("/providers/%s/missionStatus", ProviderResource.PROVIDER_ID);
    var extractable =
        given()
            .contentType("application/json")
            .body(ms)
            .when()
            .post(path)
            .then()
            .statusCode(200)
            .extract();

    // System.out.printf("asPrettyString=%s\n", extractable.asPrettyString());

    var res = extractable.jsonPath().getObject("", Mission.class);

    assertThat(res.providerId).isEqualTo(ProviderResource.PROVIDER_ID);
    assertThat(res.missionTplId).isEqualTo(ms.missionTplId);
    assertThat(res.missionId).isEqualTo(ms.missionId);
    assertThat(res.missionStatus).isEqualTo(ms.status);
  }
}
