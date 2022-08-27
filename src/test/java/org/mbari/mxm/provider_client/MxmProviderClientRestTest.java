package org.mbari.mxm.provider_client;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.graphql.ProviderPingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(
  value = WireMockProviderRest.class,
  initArgs = {
    @ResourceArg(name = "port", value = "8888"),
  }
)
@Slf4j
public class MxmProviderClientRestTest {

  static MxmProviderClient client;

  @BeforeAll
  public static void init() {
    client = MxmProviderClientBuilder.create(
      "TethysDash",
      "http://localhost:8888/api",
      ProviderApiType.REST0
    );
  }

  @AfterAll
  public static void done() {
    client.done();
  }

  @Test
  public void testPingEndpoint() throws ProviderPingException {
    var response = client.ping();
    assertNotNull(response.result.datetime);
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
    var response = client.getMissionTemplates();
    var result = response.result;
    assertEquals("", result.subDir);
    assertEquals(2, result.entries.size());
  }

  @Test
  public void testMissionTemplatesEndpoint_examples() {
    var response = client.getMissionTemplates("_examples");
    var result = response.result;
    assertEquals("_examples", result.subDir);
    assertEquals("_examples/SysLogExample.tl", result.entries.get(0).missionTplId);
  }

  @Test
  public void testMissionTemplatesEndpointScience() {
    var response = client.getMissionTemplates("Science");
    var result = response.result;
    assertEquals("Science", result.subDir);
    assertEquals(1, result.entries.size());
    assertEquals("Science/mbts_sci2.tl", result.entries.get(0).missionTplId);
  }

  @Test
  public void testMissionTemplateEndpoint() {
    var response = client.getMissionTemplate("Science/mbts_sci2.tl", false);

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
