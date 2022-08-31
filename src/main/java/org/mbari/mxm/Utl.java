package org.mbari.mxm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utl {
  public static String primaryKey(String... parts) {
    return String.join(",", parts);
  }

  /**
   * Cleans a directory or file path string so:
   * <p>
   * - single leading slash always
   * - no consecutive slashes.
   */
  public static String cleanPath(String filePath) {
    // no consecutive //
    filePath = filePath.replaceAll("//+", "/");

    // leave only one leading /
    filePath = '/' + filePath.replaceFirst("^/+", "");

    return filePath;
  }

  public static String writeJson(Object obj) {
    try {
      return objectWriter.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.warn("Failed to write JSON", e);
      return String.valueOf(obj);
    }
  }

  private static final ObjectWriter objectWriter =
      new ObjectMapper().writerWithDefaultPrettyPrinter();
}
