package com.epam.asset.tracking.mapper.converter;

import com.epam.asset.tracking.domain.Asset;
import com.fasterxml.jackson.databind.ObjectMapper;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created on 1/25/2018.
 */
@Component
public class JsonStringToAssetConverter extends CustomConverter<String, Asset>{

    /**
     * Created by Miguel Monraz on 12/02/2018
     * Converts a given JSON String into an Java Object
     * @param entity a JSON String that represents an Asset
     * @return Asset
     *
     **/

	@Override
	public Asset convert(String source, Type<? extends Asset> destinationType) {
		Asset asset = null;
		
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            asset = objectMapper.readValue(source, Asset.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return asset;
    }

}
