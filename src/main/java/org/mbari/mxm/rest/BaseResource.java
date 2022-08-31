package org.mbari.mxm.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseResource {

  private static final ObjectMapper om = new ObjectMapper();

  String writeValueAsString(Object value) {
    try {
      return om.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warn("Failed to write value as string", e);
      return String.valueOf(value);
    }
  }
}
