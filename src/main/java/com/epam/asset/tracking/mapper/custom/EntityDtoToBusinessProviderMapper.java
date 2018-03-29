package com.epam.asset.tracking.mapper.custom;

import com.epam.asset.tracking.domain.Address;
import com.epam.asset.tracking.domain.BUSINESS_TYPE;
import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.User;
import com.epam.asset.tracking.dto.UserDTO;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

/**
 * Created on 1/29/2018.
 */
@Component
public class EntityDtoToBusinessProviderMapper extends CustomMapper<UserDTO, BusinessProvider> {

    @Override
    public void mapAtoB(UserDTO dto, BusinessProvider entity, MappingContext context){
        
        Address address = new Address();
        address.setStreet(dto.getAddress());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());

        entity.setAddress(address);
        entity.setRole(User.Role.valueOf(dto.getRole()));
        entity.setType(BUSINESS_TYPE.getEnum(dto.getBusinessType()));

    }
}
