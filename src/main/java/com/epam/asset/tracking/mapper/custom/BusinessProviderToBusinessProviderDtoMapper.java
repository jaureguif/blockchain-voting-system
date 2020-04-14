package com.epam.asset.tracking.mapper.custom;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.dto.BusinessProviderDTO;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

/**
 * Created on 1/29/2018.
 */
@Component
public class BusinessProviderToBusinessProviderDtoMapper extends CustomMapper<BusinessProvider, BusinessProviderDTO> {

    @Override
    public void mapAtoB(BusinessProvider bp, BusinessProviderDTO bpDTO, MappingContext context){

        bpDTO.setBusinessType(bp.getType());

    }
}
