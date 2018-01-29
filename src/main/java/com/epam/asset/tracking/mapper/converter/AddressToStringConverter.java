package com.epam.asset.tracking.mapper.converter;

import com.epam.asset.tracking.domain.Address;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

/**
 * Created on 1/25/2018.
 */
@Component
public class AddressToStringConverter extends CustomConverter<Address, String> {

    @Override
    public String convert(Address entity, Type<? extends String> type) {
        return entity.getCity()+ " " + entity.getZipCode();
    }
}
