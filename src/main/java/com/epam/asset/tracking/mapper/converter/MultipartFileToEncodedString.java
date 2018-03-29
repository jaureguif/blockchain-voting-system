package com.epam.asset.tracking.mapper.converter;

import java.io.IOException;
import java.util.Base64;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created on 2/22/2018.
 */
@Component
public class MultipartFileToEncodedString extends CustomConverter<MultipartFile, String> {
  private static final Logger logger = LoggerFactory.getLogger(MultipartFileToEncodedString.class);

  @Override
  public String convert(MultipartFile source, Type<? extends String> destinationType) {

    String encodedSource = null;

    if (source != null && !source.isEmpty()) {

      try {
        encodedSource = Base64.getEncoder().encodeToString(source.getBytes());
      } catch (IOException ioe) {
        logger.error("Could not encode MultipartFile", source, ioe);
      }

      if (StringUtils.isEmpty(encodedSource)) {
        encodedSource = null;
      }

    } else {
      logger.error("Could not encode MultipartFile", source);
    }


    return encodedSource;

  }

}
