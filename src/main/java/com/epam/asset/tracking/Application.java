package com.epam.asset.tracking;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
<<<<<<< HEAD

import com.epam.asset.tracking.annotation.CoverageIgnore;
=======
>>>>>>> d9dfe09f45935f85a0696d0d7a22e11765520849

@SpringBootApplication
@EnableResourceServer
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.epam.asset.tracking")
@EnableMongoRepositories(basePackages = "com.epam.asset.tracking.repository")
public class Application {
	Logger log = LoggerFactory.getLogger(Application.class);

	@CoverageIgnore
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			log.info("Beans defined in the current context:");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				log.info(beanName);
			}

		};
	}
}