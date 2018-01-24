package com.epam.asset.tracking.integration.entity;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.integration.AbstractIntegrationTest;

public class EntityIntegrationTest extends AbstractIntegrationTest {
	
	@Test
	public void shouldReturn201() throws Exception {
		
		
		mockMvc.perform(post("/asset/tracking/entity/")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().string(containsString("Hello world")));
	}
	

}
