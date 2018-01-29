package com.epam.asset.tracking.mapper.custom;

import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.entities.AnEntity;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

/**
 * Created  on 1/25/2018.
 */
@Component
public class EntityDtoToAnEntityMapper extends CustomMapper <EntityDTO, AnEntity> {

    @Override
    public void mapAtoB(EntityDTO dto, AnEntity entity, MappingContext context){


    }

}
