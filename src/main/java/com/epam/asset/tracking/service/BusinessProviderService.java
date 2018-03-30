package com.epam.asset.tracking.service;

import java.util.Optional;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;

public interface BusinessProviderService {
	
	public BusinessProvider save(BusinessProvider entity);

	public Optional<BusinessProvider> findUserbyUsername(String name);

	public void generatePasswordAndSendEmail(String username) throws InvalidUserException;
}
