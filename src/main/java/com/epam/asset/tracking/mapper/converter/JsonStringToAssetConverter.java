package com.epam.asset.tracking.mapper.converter;

import java.io.IOException;

import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.exception.AssetConvertException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created on 1/25/2018.
 */
@Component
public class JsonStringToAssetConverter extends CustomConverter<String, Asset> {

  /**
   * Created by Miguel Monraz on 12/02/2018 Converts a given JSON String into an Java Object
   *
   * @param source a JSON String that represents an Asset
   * @return Asset
   *
   **/
  private Logger logger = LoggerFactory.getLogger(JsonStringToAssetConverter.class);

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Asset convert(String source, Type<? extends Asset> destinationType)
      throws AssetConvertException {
    Asset asset = null;

    if (!StringUtils.isEmpty(source)) {
      try {
        asset = objectMapper.readValue(source, Asset.class);
      } catch (IOException ioe) {
        logger.error("Not able to convert provide JSON to an Asset representation", ioe);
        throw new AssetConvertException(
            "Not able to convert provide JSON to an Asset representation", ioe);
      }
    }

    return asset;
  }

}
