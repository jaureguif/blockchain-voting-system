package com.epam.asset.tracking.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.epam.asset.tracking.domain.BusinessProvider;

public interface BusinessProviderRepository extends MongoRepository<BusinessProvider, String> {
	
	public BusinessProvider findByName(String name);

	public BusinessProvider findByUsername(String name);

}
