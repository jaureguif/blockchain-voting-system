package com.epam.asset.tracking.web.balance;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matcher;
import org.hamcrest.text.IsEmptyString;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.EntityService;
import com.epam.asset.tracking.web.AbstractWebTest;


public class ControllerTest extends AbstractWebTest{
	
	@MockBean
	ApiService api;
	
	@MockBean
	EntityService entity;

	//@Test
	public void shouldReturnDefaultMessage() throws Exception {
		mockMvc.perform(get("/asset/tracking/entity/hello")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Hello world")));
	}
	
	@Test
	public void shouldReturn201() throws Exception {
		
		mockMvc.perform(post("/asset/tracking/entity/")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().string(IsEmptyString.isEmptyString()));
	}


}


