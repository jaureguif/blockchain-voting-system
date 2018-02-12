package com.epam.asset.tracking.mapper.converter;

import com.epam.asset.tracking.domain.Asset;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonToAssetConverterTest {

    @Test
    public void convertJSONStringToAsset(){

        String jsonAsset = "{\"id\": \"1\",\"name\": \"vehicle\", \"description\": \"figo 2018\"}";

        JsonToAssetConverter converter = new JsonToAssetConverter();


        Asset asset = null;

        //Converts the given JSON Script into an Asset Object
        asset  = converter.convert(jsonAsset);

        //Check if the values from the Asset object created matched the on the JSON String
        assertEquals(asset.getId(), "1");
        assertEquals(asset.getName(), "vehicle");
        assertEquals(asset.getDescription(), "figo 2018");


    }
}
