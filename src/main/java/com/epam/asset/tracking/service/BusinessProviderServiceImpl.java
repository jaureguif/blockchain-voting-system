package com.epam.asset.tracking.service;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.util.EmailSender;
import com.epam.asset.tracking.util.RandomPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BusinessProviderServiceImpl implements BusinessProviderService {

	@Autowired
	private BusinessProviderRepository repository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	EmailSender emailSender;

	@Autowired
	private RandomPasswordGenerator passwordGenerator;

	@Override
	public BusinessProvider save(BusinessProvider entity) {
		//encode password
		entity.setPassword(bCryptPasswordEncoder.encode(entity.getPassword()));
		return repository.save(entity);
	}

	@Override
	public Optional<BusinessProvider> findUserbyUsername(String name) {
		return repository.findByUsername(name);
	}

	@Override
	public void generatePasswordAndSendEmail(String username) throws InvalidUserException {
		String newPassword = passwordGenerator.generateNewPassword();

		BusinessProvider userData = findUserbyUsername(username).orElseThrow(() -> new InvalidUserException("Invalid username provided"));

		updatePassword(userData, newPassword);

		emailSender.sendEmail(userData.getEmail(), userData.getName(), newPassword);
	}

	@Override
	public BusinessProvider updatePassword(BusinessProvider entity, String password) {
		entity.setPassword(bCryptPasswordEncoder.encode(password));
		return repository.save(entity);
	}




}
