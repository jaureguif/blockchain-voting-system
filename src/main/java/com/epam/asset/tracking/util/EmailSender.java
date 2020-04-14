package com.epam.asset.tracking.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

    private final JavaMailSender emailSender;

    public @Autowired EmailSender(JavaMailSender emailSender) {
      this.emailSender = emailSender;
    }

    public void sendEmail(String email, String name, String newPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("New password Generated:");
        message.setText("Hi " + name + "\n\nThis is your generated password: " + newPassword);
        emailSender.send(message);
    }
}
