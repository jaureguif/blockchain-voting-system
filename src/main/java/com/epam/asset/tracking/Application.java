package com.epam.asset.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication
@EnableAutoConfiguration
@EnableMongoRepositories(basePackages="com.epam.asset.tracking.repository")
@Configuration 
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		
	}
}