package org.mbari.mxm.provider_client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.graphql.ProviderPingException;
import org.mbari.mxm.provider_client.rest.MxmInfo;

@QuarkusTest
@QuarkusTestResource(
    value = WireMockProviderRest.class,
    initArgs = {
      @ResourceArg(name = "port", value = "8888"),
    })
@Slf4j
public class MxmProviderClientRestTest {

  static MxmProviderClient client;

  @BeforeAll
  public static void init() {
    client =
        MxmProviderClientBuilder.create(
            "TethysDash", "http://localhost:8888/api", ProviderApiType.REST);
  }

  @AfterAll
  public static void done() {
    client.done();
  }

  @Test
  public void testPingEndpoint() throws ProviderPingException {
    var mxmInfo = new MxmInfo();
    mxmInfo.mxmRestEndpoint = "http://foo.example/api";
    var response = client.ping(mxmInfo);
    assertNotNull(response.result.datetime);

    var datetime = OffsetDateTime.parse(response.result.datetime);
    assertThat(datetime).isAfter(OffsetDateTime.now().minusSeconds(10));
  }

  @Test
  public void testGeneralInfoEndpoint() {
    var response = client.getGeneralInfo();
    var result = response.result;
    assertEquals("TethysDash", result.providerName);
    assertEquals("TethysDash/LRAUV System", result.providerDescription);
  }

  @Test
  public void testAssetClassesEndpoint() {
    var response = client.getAssetClasses();
    var result = response.result;
    assertEquals(1, result.size());
    var ac = result.get(0);
    assertEquals("LRAUV", ac.assetClassName);
    assertEquals(3, ac.assets.size());
    assertEquals("Long-Range Autonomous Underwater Vehicle", ac.description);
  }

  @Test
  public void testMissionTemplatesEndpoint() {
    var response = client.getMissionTemplates("/");
    var result = response.result;
    // log.warn(Utl.writeJson(result));

    assertEquals("", result.directory);
    assertEquals(3, result.entries.size());
    assertEquals("Default.xml", result.entries.get(0).missionTplId);
    assertEquals(List.of("LRAUV"), result.entries.get(0).assetClassNames);

    assertEquals("Science/", result.entries.get(1).missionTplId);

    assertEquals("_examples/", result.entries.get(2).missionTplId);
    assertEquals(3, result.entries.get(2).entries.size());
    assertEquals("_examples/SysLogExample.tl", result.entries.get(2).entries.get(0).missionTplId);
    assertEquals("FOO", result.entries.get(2).entries.get(1).description);
    assertEquals(1, result.entries.get(2).entries.get(2).entries.size());
    assertEquals(
        "_examples/subdir/baz.tl",
        result.entries.get(2).entries.get(2).entries.get(0).missionTplId);
  }

  @Test
  public void testMissionTemplatesEndpoint_examples() {
    var response = client.getMissionTemplates("_examples");
    var result = response.result;
    assertEquals("_examples", result.directory);
    assertEquals("_examples/SysLogExample.tl", result.entries.get(0).missionTplId);
  }

  @Test
  public void testMissionTemplatesEndpointScience() {
    var response = client.getMissionTemplates("Science");
    var result = response.result;
    assertEquals("Science", result.directory);
    assertEquals(1, result.entries.size());
    assertEquals("Science/mbts_sci2.tl", result.entries.get(0).missionTplId);
  }

  @Test
  public void testMissionTemplateEndpoint() {
    var response = client.getMissionTemplate("Science/mbts_sci2.tl");

    var result = response.result;
    assertEquals("Science/mbts_sci2.tl", result.missionTplId);
    assertEquals(1, result.assetClassNames.size());
    assertEquals("LRAUV", result.assetClassNames.get(0));
    assertNotNull(result.description);
    assertEquals(7, result.parameters.size());
  }

  @Test
  public void testUnitsEndpoint() {
    var response = client.getUnits();
    var result = response.result;
    assertEquals(7, result.size());
    var unit = result.get(1);
    assertEquals("ampere_hour", unit.name);
    assertEquals("Ah", unit.abbreviation);
    assertEquals("ampere_second", unit.baseUnit);
  }
}
