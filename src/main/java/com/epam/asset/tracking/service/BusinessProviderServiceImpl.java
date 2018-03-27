package com.epam.asset.tracking.service;

import com.epam.asset.tracking.exception.InvalidUserException;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.repository.BusinessProviderRepository;

import java.util.Optional;

@Component
public class BusinessProviderServiceImpl implements BusinessProviderService {

	@Autowired
	private BusinessProviderRepository repository;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	BusinessProviderService businessProviderService;

	@Autowired
	JavaMailSender emailSender;

	private String newPassword;

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
	public BusinessProvider updatePassword(BusinessProvider entity, String password) {
		entity.setPassword(bCryptPasswordEncoder.encode(password));
		return repository.save(entity);
	}

	@Override
	public BusinessProvider generateNewPassword(String username) throws InvalidUserException {
		PasswordGenerator passwordGenerator = new PasswordGenerator();

		CharacterRule characterRule = new CharacterRule(EnglishCharacterData.Alphabetical);

		newPassword = passwordGenerator.generatePassword(8, characterRule);

		BusinessProvider userData = businessProviderService.findUserbyUsername(username).orElseThrow(() -> new InvalidUserException("Invalid username provided"));

		businessProviderService.updatePassword(userData, newPassword);

		return userData;


	}

	@Override
	public void sendEmail(BusinessProvider user) {

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user.getEmail());
		message.setSubject("New password Generated:");
		message.setText("Hi "+user.getName()+"\n\nThis is your generated password: "+newPassword);

		emailSender.send(message);

	}


}
