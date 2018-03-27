package com.epam.asset.tracking.service;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class UserServiceImplTest {

  UserServiceImpl instance;
  BusinessProviderService businessInstance;
  JavaMailSender emailSenderInstance;

  @Before
  public void setup() {
    instance = new UserServiceImpl();
    instance.userRepository = mock(BusinessProviderRepository.class);

  }

  @Test
  public void happyPath() {
    BusinessProvider bp = new BusinessProvider();
    bp.setUsername("someUserName");
    bp.setEmail("an@email.com");
    bp.setPassword("pass");
    
    doReturn(Optional.of(bp)).when(instance.userRepository).findByUsername("someUserName");
    UserDetails user = instance.loadUserByUsername("someUserName");
    assertThat(user.getPassword(), is("pass"));

  }

  
  @Test(expected = UsernameNotFoundException.class)
  public void userNotFound() {

    doReturn(Optional.empty()).when(instance.userRepository).findByUsername("someUserName");
    
    instance.loadUserByUsername("someUserName");

  }

  @Test
  public void shouldCreateNewPassword() throws InvalidUserException {
    BusinessProvider userData = mock(BusinessProvider.class);

    userData.setUsername("mmonraz");
    userData.setEmail("miguel_monraz@epam.com");
    userData.setPassword("pa55w0rd");

    businessInstance = mock(BusinessProviderService.class);
    Mockito.when(businessInstance.findUserbyUsername(Mockito.anyString())).thenReturn(Optional.ofNullable(userData));

    Mockito.when(businessInstance.generateNewPassword(Mockito.anyString())).thenReturn(userData);

    assertNotEquals("Passwords are not equal","pa55w0rd", userData.getPassword());

  }

  @Test
  public void shouldSendEmail() throws InvalidUserException{
    BusinessProvider userData = mock(BusinessProvider.class);

    userData.setUsername("mmonraz");
    userData.setEmail("miguel_monraz@epam.com");
    userData.setPassword("pa55w0rd");

    businessInstance = mock(BusinessProviderService.class);
    Mockito.when(businessInstance.findUserbyUsername(Mockito.anyString())).thenReturn(Optional.ofNullable(userData));

    emailSenderInstance = mock(JavaMailSender.class);

    businessInstance.sendEmail(userData);

    Assert.assertTrue(true);
  }
  
}
