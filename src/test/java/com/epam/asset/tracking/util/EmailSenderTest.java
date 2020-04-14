package com.epam.asset.tracking.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@RunWith(MockitoJUnitRunner.class)
public class EmailSenderTest {

  private @Mock JavaMailSender javaMailSender;
  private @InjectMocks EmailSender emailSender;

  @Test
  public void shouldCallEmailSenderSendMethodWithParamerts() {
    String email = "user@company.com";
    String name = "user";
    String password = "p455w0d";
    emailSender.sendEmail(email, name, password);
    ArgumentCaptor<SimpleMailMessage> argCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(javaMailSender).send(argCaptor.capture());
    SimpleMailMessage message = argCaptor.getValue();
    assertThat(message).isNotNull();
    assertThat(message.getTo()).isNotNull().isNotEmpty().containsExactly(email);
    assertThat(message.getText()).isNotBlank().contains(name, password);
  }
}
