package com.epam.asset.tracking.web;

import com.epam.asset.tracking.api.UserController;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(secure = false)
public abstract class AbstractWebTest {

	@Autowired
	protected MockMvc mockMvc;
	protected ObjectMapper jacksonMapper = new ObjectMapper();

}
