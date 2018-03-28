package com.epam.asset.tracking.service;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;

import java.util.Optional;

public interface BusinessProviderService {
	
	public BusinessProvider save(BusinessProvider entity);

	public Optional<BusinessProvider> findUserbyUsername(String name);

	public void generatePasswordAndSendEmail(String username) throws InvalidUserException;

	public BusinessProvider updatePassword(BusinessProvider entity, String password);

}
