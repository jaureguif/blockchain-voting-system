package com.epam.asset.tracking.mapper.converter;

import com.epam.asset.tracking.entities.AnEntity;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

/**
 * Created on 1/25/2018.
 */
@Component
public class AnEntityToStringConverter extends CustomConverter<AnEntity, String> {

    @Override
    public String convert(AnEntity entity, Type<? extends String> type) {
        String converted = "";
        return entity.getCity()+ " " + entity.getZipCode();
    }
}
