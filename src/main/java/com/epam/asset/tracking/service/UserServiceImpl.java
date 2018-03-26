package com.epam.asset.tracking.service;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.epam.asset.tracking.repository.BusinessProviderRepository;

@Service(value = "userService")
public class UserServiceImpl implements UserDetailsService, UserService {

  @Autowired
  BusinessProviderRepository userRepository;

  @Autowired
  JavaMailSender emailSender;

  @Autowired
  BusinessProviderService businessProviderService;

  private String newPassword;

  @Override
  public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
    return userRepository.findByUsername(userName)
        .map(user -> new org.springframework.security.core.userdetails.User(user.getUsername(),
            user.getPassword(), getAuthority()))
        .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
  }

  private List<GrantedAuthority> getAuthority() {
    return Arrays.asList(new SimpleGrantedAuthority("ROLE_BUSINESS_PROVIDER"));
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
  public void sendEmail(BusinessProvider user) throws InvalidUserException{

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(user.getEmail());
    message.setSubject("New password Generated:");
    message.setText("Hi "+user.getName()+"\n\nThis is your generated password: "+newPassword);

    emailSender.send(message);

  }


  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost("smtp.gmail.com");
    mailSender.setPort(587);

    mailSender.setUsername("blockchainepam@gmail.com");
    mailSender.setPassword("blockchain123");

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");

    return mailSender;
  }
}
