package com.epam.asset.tracking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.exception.InvalidUserException;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.util.EmailSender;
import com.epam.asset.tracking.util.RandomPasswordGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RunWith(MockitoJUnitRunner.class)
public class BusinessProviderServiceImplTest {

  private static final String ENCRYPTED = "encrypted";
  private static final String RANDOM_PASSWORD = "random";

  private @Mock BusinessProviderRepository repository;
  private @Mock BCryptPasswordEncoder passwordEncoder;
  private @Mock EmailSender emailSender;
  private @Mock RandomPasswordGenerator passwordGenerator;
  private @InjectMocks BusinessProviderServiceImpl service;

  @Before
  public void onBeforeEachTest() {
    when(passwordGenerator.generateNewPassword()).thenReturn(RANDOM_PASSWORD);
    when(passwordEncoder.encode(any(CharSequence.class))).thenReturn(ENCRYPTED);
    when(repository.save(any(BusinessProvider.class))).then(invocation -> invocation.getArgumentAt(0, BusinessProvider.class));
  }

  @Test
  public void shouldSaveEntityAndEncryptPassword() {
    BusinessProvider arg = new BusinessProvider();
    arg.setUsername("bp1");
    arg.setPassword("password");

    BusinessProvider actual = service.save(arg);
    assertThat(actual).isNotNull().extracting(BusinessProvider::getUsername, BusinessProvider::getPassword).containsExactly(arg.getUsername(), ENCRYPTED);
  }

  @Test
  public void shouldFindUserByUsernameReturnNonEmptyOptionalWhenRepositoryFindsSomething() {
    BusinessProvider businessProvider = new BusinessProvider();
    businessProvider.setUsername("bp1");
    businessProvider.setPassword("password");
    when(repository.findByUsername(businessProvider.getUsername())).thenReturn(Optional.of(businessProvider));

    Optional<BusinessProvider> actual = service.findUserbyUsername("bp1");
    assertThat(actual).isNotNull().isNotEqualTo(Optional.empty());
    assertThat(actual.isPresent()).isTrue();
    assertThat(actual.get()).isEqualTo(businessProvider);
  }

  @Test
  public void shouldFindUserByUsernameReturnEmptyOptionalWhenRepositoryFindsNothing() {
    when(repository.findByUsername(anyString())).thenReturn(Optional.empty());

    Optional<BusinessProvider> actual = service.findUserbyUsername("bp1");
    assertThat(actual).isNotNull().isEqualTo(Optional.empty());
    assertThat(actual.isPresent()).isFalse();
  }

  @Test
  public void shouldGeneratePasswordAndSendEmailWhenRepositoryFindsSomething() {
    BusinessProvider entity = new BusinessProvider();
    entity.setName("GenericBusinessProvider");
    entity.setUsername("bp1");
    entity.setPassword("password");
    entity.setEmail("bp1@company.com");
    when(repository.findByUsername(entity.getUsername())).thenReturn(Optional.of(entity));

    try {
      service.generatePasswordAndSendEmail(entity.getUsername());
    } catch (InvalidUserException e) {
      fail("Unexpected exception", e);
    }

    ArgumentCaptor<String> emailSenderCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<BusinessProvider> saveCaptor = ArgumentCaptor.forClass(BusinessProvider.class);
    verify(repository).save(saveCaptor.capture());
    verify(emailSender).sendEmail(emailSenderCaptor.capture(), emailSenderCaptor.capture(), emailSenderCaptor.capture());

    assertThat(saveCaptor.getValue()).isNotNull()
                                     .extracting(BusinessProvider::getName, BusinessProvider::getUsername, BusinessProvider::getPassword, BusinessProvider::getEmail)
                                     .containsExactly(entity.getName(), entity.getUsername(), ENCRYPTED, entity.getEmail());
    assertThat(emailSenderCaptor.getAllValues()).isNotNull().isNotEmpty().containsExactly(entity.getEmail(), entity.getName(), RANDOM_PASSWORD);
  }

  @Test
  public void shouldGeneratePasswordAndSendEmailThrowInvalidUserExceptionWhenRepositoryFindsNothing() {
    when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.generatePasswordAndSendEmail("any")).isInstanceOf(InvalidUserException.class).hasMessage("Invalid username provided");
  }
}
