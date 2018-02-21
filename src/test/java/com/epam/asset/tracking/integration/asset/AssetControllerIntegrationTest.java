package com.epam.asset.tracking.integration.asset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.integration.AbstractIntegrationTest;

public class AssetControllerIntegrationTest extends AbstractIntegrationTest {

	
	@Test
	public void shouldReturn200() throws Exception {
		mockMvc.perform(get("/asset/tracking/asset/9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d3")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk());
	
	}
	
	@Test
	public void shouldReturn404() throws Exception {
		mockMvc.perform(get("/asset/tracking/asset/9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d2")
				.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNotFound());
	
	}
	
	
}
