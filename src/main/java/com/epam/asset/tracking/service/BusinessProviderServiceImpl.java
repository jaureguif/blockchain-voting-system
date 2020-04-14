package com.epam.asset.tracking.service;

import java.util.Optional;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.util.EmailSender;
import com.epam.asset.tracking.util.RandomPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BusinessProviderServiceImpl implements BusinessProviderService {

  private final BusinessProviderRepository repository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final EmailSender emailSender;
  private final RandomPasswordGenerator passwordGenerator;

  public @Autowired BusinessProviderServiceImpl(
      BusinessProviderRepository repository,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      EmailSender emailSender, RandomPasswordGenerator passwordGenerator) {
    this.repository = repository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.emailSender = emailSender;
    this.passwordGenerator = passwordGenerator;
  }

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
    BusinessProvider entity = findUserbyUsername(username)
        .orElseThrow(() -> new InvalidUserException("Invalid username provided"));
    String newPassword = passwordGenerator.generateNewPassword();
    entity.setPassword(newPassword);
    save(entity);
    emailSender.sendEmail(entity.getEmail(), entity.getName(), newPassword);
  }
}
