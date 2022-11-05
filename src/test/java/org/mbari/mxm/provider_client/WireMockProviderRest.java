package org.mbari.mxm.provider_client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.provider_client.responses.PingResponse;

@Slf4j
public class WireMockProviderRest implements QuarkusTestResourceLifecycleManager {
  private static final String BASE_API_PATH = "/api";

  private static final String BASE_RES_PATH = "/mxm_client_responses/mxm";

  private int port = 8888;

  private WireMockServer wireMockServer;

  @Override
  public void init(Map<String, String> initArgs) {
    port = Integer.parseInt(initArgs.getOrDefault("port", "8888"));
    log.debug("WireMockProviderRest.init: initArgs={}, using port={}", initArgs, port);
  }

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer(port);
    wireMockServer.start();
    addStubs();
    return Collections.singletonMap(
        "quarkus.rest-client.\"org.mbari.mxm.provider_client.ProviderClientService\".url",
        wireMockServer.baseUrl() + BASE_API_PATH);
  }

  @Override
  public void stop() {
    if (Objects.nonNull(wireMockServer)) {
      wireMockServer.stop();
    }
  }

  private void addStubs() {
    try {
      addPingStub();
      addStub("/info");
      addStub("/missiontemplates");
      addStub("/missiontemplates/_examples", BASE_RES_PATH + "/missiontemplates_examples.json");
      addStub("/missiontemplates/Science", BASE_RES_PATH + "/missiontemplatesScience.json");
      addStub("/missiontemplate/Science/mbts_sci2.tl");
      addStub("/assetclasses");
      addStub("/units");
    } catch (IOException e) {
      fail("Could not configure Wiremock server. Caused by: " + e.getMessage());
    }
  }

  private void addPingStub() throws IOException {
    var pingResp = new PingResponse();
    pingResp.result = new PingResponse.Pong();
    pingResp.result.datetime = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    wireMockServer.stubFor(
        post(urlEqualTo(BASE_API_PATH + "/ping")).willReturn(jsonResponse(pingResp, 200)));
  }

  private void addStub(String suffix) throws IOException {
    addStub(suffix, BASE_RES_PATH + suffix + ".json");
  }

  private void addStub(String suffix, String resName) throws IOException {
    String resContent = getResource(resName);
    log.debug("resName={} => resContent={}", resName, resContent);

    wireMockServer.stubFor(get(urlEqualTo(BASE_API_PATH + suffix)).willReturn(okJson(resContent)));
  }

  private String getResource(String resName) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(resName)) {
      assert is != null;
      return new String(is.readAllBytes());
    }
  }
}
