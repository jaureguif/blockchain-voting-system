package com.epam.asset.tracking.mapper.converter;

import com.epam.asset.tracking.domain.Asset;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created on 1/25/2018.
 */
@Component
public class JsonToAssetConverter {

    /**
     * Created by Miguel Monraz on 12/02/2018
     * Converts a given JSON String into an Java Object
     * @param entity a JSON String that represents an Asset
     * @return Asset
     *
     **/

    public Asset convert(String entity) {

        ObjectMapper objectMapper = new ObjectMapper();

        Asset asset = null;
        try {
            asset = objectMapper.readValue(entity, Asset.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return asset;
    }
}
