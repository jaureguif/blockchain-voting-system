package com.epam.asset.tracking.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {


    public void sendEmail(String email, String name, String newPasword) {

        JavaMailSender emailSender = new JavaMailSenderImpl();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("New password Generated:");
        message.setText("Hi "+name+"\n\nThis is your generated password: "+newPasword);

        emailSender.send(message);

    }
}
