package com.epam.asset.tracking.mapper.custom;

import com.epam.asset.tracking.domain.Address;
import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.User;
import com.epam.asset.tracking.dto.EntityDTO;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

/**
 * Created on 1/29/2018.
 */
@Component
public class EntityDtoToBusinessProviderMapper extends CustomMapper<EntityDTO, BusinessProvider> {

    @Override
    public void mapAtoB(EntityDTO dto, BusinessProvider entity, MappingContext context){
        entity.setEmail(dto.getMail());

        Address address = new Address();
        address.setCity(dto.getCity());
        address.setZipCode(dto.getZipCode());

        entity.setAddress(address);
        entity.setRole(User.Role.valueOf(dto.getRole()));
        entity.setType(BusinessProvider.Type.valueOf(dto.getBusinessType()));

    }
}
