package com.epam.asset.tracking.web;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.epam.asset.tracking.Application;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc
public abstract class AbstractWebTest {
	
	@Autowired
	protected MockMvc mockMvc;

}
