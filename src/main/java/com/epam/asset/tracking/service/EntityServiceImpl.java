package com.epam.asset.tracking.service;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Service;

import com.epam.asset.tracking.dto.EntityDTO;

@Service
public class EntityServiceImpl implements EntityService{

	@Override
	public void newEntity(EntityDTO entity) {
		throw new NotImplementedException();
		
	}

}
