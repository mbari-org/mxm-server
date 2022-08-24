package org.mbari.mxm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.HashMap;
import java.util.Map;

@lombok.extern.slf4j.Slf4j
public abstract class BaseForTests {

  protected static String bodyForRequest(String s) throws JsonProcessingException {
    return bodyForRequest(s, null);
  }

  protected static String bodyForRequest(String s, Map<String, Object> variables) throws JsonProcessingException {
    Map<String, Object> body = new HashMap<>();
    body.put("query", s.trim());
    if (variables != null) {
      body.put("variables", variables);
    }
    return objectWriter.writeValueAsString(body);
  }

  protected static final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
}
