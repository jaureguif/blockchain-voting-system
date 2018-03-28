package com.epam.asset.tracking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class EmailConfiguration {

    @Autowired
    Environment environment;

    @Bean
    public JavaMailSender getJavaMailSender() throws IOException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(environment.getProperty("spring.mail.host"));
        mailSender.setPort(Integer.parseInt(environment.getProperty("spring.mail.port")));
        mailSender.setUsername(environment.getProperty("spring.mail.username"));
        mailSender.setPassword(environment.getProperty("spring.mail.password"));


        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", environment.getProperty("mail.transport.protocol"));
        props.put("mail.smtp.auth", environment.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", environment.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.debug", environment.getProperty("mail.debug"));

        mailSender.setJavaMailProperties(props);

        return mailSender;
    }
}
