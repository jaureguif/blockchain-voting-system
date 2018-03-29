package com.epam.asset.tracking.repository;

import java.util.Optional;

import com.epam.asset.tracking.domain.BusinessProvider;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BusinessProviderRepository extends MongoRepository<BusinessProvider, String> {
	
	public Optional<BusinessProvider> findByUsername(String name);
	
	

}
