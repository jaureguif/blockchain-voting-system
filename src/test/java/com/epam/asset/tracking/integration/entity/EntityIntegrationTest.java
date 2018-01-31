package com.epam.asset.tracking.integration.entity;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.integration.AbstractIntegrationTest;

public class EntityIntegrationTest extends AbstractIntegrationTest {

	String username;
	
	@Before
	public void setup() {
		
		username = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("[0-9]", "");
		
	}
	
	@Test
	public void shouldReturn201() throws Exception {

		EntityDTO dto = new EntityDTO();
		dto.setAddress(
				"string with a comma, not at the end, of course, but with a period at the end. and one more thing...");
		dto.setBusinessType("car mechanic");
		dto.setCity("CityTEST");
		dto.setLastName("lastNameTest");
		dto.setEmail("foo@mail.com");
		dto.setName("NameTest");
		dto.setPassword("password1");
		dto.setRfc("rfc");
		dto.setState("State TEST");
		dto.setZipCode("12345");
		System.out.println("USERNAME: " + username);
		dto.setUsername(username);
		
		System.out.println(jacksonMapper.writeValueAsString(dto));

		mockMvc.perform(post("/asset/tracking/entity/")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jacksonMapper.writeValueAsString(dto)))
				.andDo(print())
				.andExpect(status().isCreated());
	}

}
