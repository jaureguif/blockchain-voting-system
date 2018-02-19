package com.epam.asset.tracking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.repository.BusinessProviderRepository;

@Component
public class BusinessProviderServiceImpl implements BusinessProviderService{

	@Autowired
	private BusinessProviderRepository repository;
	
	@Override
	public BusinessProvider save(BusinessProvider entity) {
		 return repository.save(entity);
	}

}
