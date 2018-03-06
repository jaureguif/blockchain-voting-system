package com.epam.asset.tracking.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.epam.asset.tracking.domain.BusinessProvider;

public interface BusinessProviderRepository extends MongoRepository<BusinessProvider, String> {
	
	public Optional<BusinessProvider> findByUsername(String name);
	
	

}
