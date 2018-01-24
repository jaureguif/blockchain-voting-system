package com.epam.asset.tracking.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan("com.epam.asset.tracking")
@EnableWebMvc
@TestConfiguration
public class EntityConfigTest {

}


