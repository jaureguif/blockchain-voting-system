package com.epam.asset.tracking.mapper.converter.serialize;

import java.io.IOException;
import java.time.ZonedDateTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {

  @Override
  public void serialize(ZonedDateTime arg0, JsonGenerator arg1, SerializerProvider arg2)
      throws IOException {
    arg1.writeString(arg0.toString());
  }

}

