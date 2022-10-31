package org.mbari.mxm.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.BaseForTests;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionSchedType;
import org.mbari.mxm.db.mission.MissionStatusType;
import org.mbari.mxm.db.missionTemplate.MissionTemplateCreatePayload;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderApiType;

@QuarkusTest
public class ProviderResourceTest extends BaseForTests {

  final String providerId = "PROVIDER_ID1";
  final String missionTplId = "MISSION_TEMPLATE_ID1";
  final String missionId = "MISSION_ID1";
  final String providerMissionId = "MISSION_PROVIDER_ID1";

  final Provider provider =
      new Provider(
          providerId,
          String.format("https://%s.net", providerId),
          ProviderApiType.REST,
          "originalDescription",
          null,
          null,
          null,
          null,
          null);

  final MissionTemplateCreatePayload missionTemplateCreatePayload =
      MissionTemplateCreatePayload.builder()
          .missionTplId(missionTplId)
          .description("missionTemplateDescription")
          .build();

  final Mission missionCreatePayload =
      Mission.builder()
          .providerId(providerId)
          .missionTplId(missionTplId)
          .missionId(missionId)
          .providerMissionId(providerMissionId)
          .assetId("acme1")
          .missionStatus(MissionStatusType.SUBMITTED)
          .schedType(MissionSchedType.ASAP)
          .description("missionDescription")
          .build();

  private void postProvider() {
    given()
        .contentType("application/json")
        .body(provider)
        .when()
        .post("/providers")
        .then()
        .statusCode(201);
  }

  private void deleteProvider() {
    given().when().delete("/providers/{providerId}", providerId).then().statusCode(200);
  }

  private void postMissionTemplate() {
    given()
        .contentType("application/json")
        .body(missionTemplateCreatePayload)
        .when()
        .post("/providers/{providerId}/missionTemplates", providerId)
        .then()
        .statusCode(201);
  }

  private void deleteMissionTemplate() {
    given()
        .contentType("application/json")
        .when()
        .delete("/providers/{providerId}/missionTemplates/{missionTplId}", providerId, missionTplId)
        .then()
        .statusCode(200);
  }

  private void postMission() {
    var res =
        given()
            .contentType("application/json")
            .body(missionCreatePayload)
            .when()
            .post(
                "/providers/{providerId}/missionTemplates/{missionTplId}/missions",
                providerId,
                missionTplId)
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getObject("", Mission.class);

    assertThat(res.providerId).isEqualTo(providerId);
    assertThat(res.missionTplId).isEqualTo(missionTplId);
    assertThat(res.missionId).isEqualTo(missionId);
    assertThat(res.missionStatus).isEqualTo(MissionStatusType.SUBMITTED);
  }

  private void putMissionStatus(MissionStatusType missionStatus) {
    var ms = new MissionStatus();
    ms.status = missionStatus;

    var extractable =
        given()
            .contentType("application/json")
            .body(ms)
            .when()
            .put(
                "/providers/{providerId}/missionTemplates/{missionTplId}/missions/{missionId}/status",
                providerId,
                missionTplId,
                missionId)
            .then()
            .statusCode(200)
            .extract();

    // System.out.printf("asPrettyString=%s\n", extractable.asPrettyString());

    var res = extractable.jsonPath().getObject("", Mission.class);

    assertThat(res.providerId).isEqualTo(providerId);
    assertThat(res.missionTplId).isEqualTo(missionTplId);
    assertThat(res.missionId).isEqualTo(missionId);
    assertThat(res.missionStatus).isEqualTo(missionStatus);
  }

  private void deleteMission() {
    given()
        .contentType("application/json")
        .when()
        .delete(
            "/providers/{providerId}/missionTemplates/{missionTplId}/missions/{missionId}",
            providerId,
            missionTplId,
            missionId)
        .then()
        .statusCode(200);
  }

  @Test
  public void sequence() {
    postProvider();
    postMissionTemplate();

    postMission();

    putMissionStatus(MissionStatusType.QUEUED);
    putMissionStatus(MissionStatusType.RUNNING);
    putMissionStatus(MissionStatusType.COMPLETED);

    deleteMission();
    deleteMissionTemplate();
    deleteProvider();
  }
}
