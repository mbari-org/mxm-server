package org.mbari.mxm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utl {
  public static String primaryKey(String... parts) {
    return String.join(",", parts);
  }

  /**
   * Cleans a directory or file path string so: single leading slash always, and no consecutive
   * slashes.
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
      JsonMapper.builder()
          .addModule(new JavaTimeModule())
          .build()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .setDateFormat(new StdDateFormat())
          .writerWithDefaultPrettyPrinter();
}
