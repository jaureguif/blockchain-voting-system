package com.epam.asset.tracking.integration.entity;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.hamcrest.text.IsEmptyString;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.integration.AbstractIntegrationTest;
import com.epam.asset.tracking.service.BusinessProviderService;

public class EntityIntegrationTest extends AbstractIntegrationTest {

	String username;
	
	
	@SpyBean
	BusinessProviderService businessProviderService;
	
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
				.with(httpBasic("admin","admin"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jacksonMapper.writeValueAsString(dto)))
				.andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	public void shouldReturn409() throws Exception {
		
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
				.with(httpBasic("admin","admin"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(jacksonMapper.writeValueAsString(dto)))
				.andDo(print())
				.andExpect(status().isCreated());

		//Will insert a new DTO, but with same username
		dto = new EntityDTO();
		dto.setAddress("New address");
		dto.setBusinessType("car mechanic");
		dto.setCity("CityTEST");
		dto.setLastName("lastNameTest");
		dto.setEmail("foo@mail.com");
		dto.setName("Another");
		dto.setPassword("password1");
		dto.setRfc("rfc");
		dto.setState("State TEST");
		dto.setZipCode("90210");
		System.out.println("SAME USERNAME: " + username);
		dto.setUsername(username);
				
		System.out.println(jacksonMapper.writeValueAsString(dto));
		
		doThrow(new DuplicateKeyException("Duplicated username"))
			.when(businessProviderService).save(any());
		
		mockMvc.perform(post("/asset/tracking/entity/")
				.with(httpBasic("admin","admin"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(jacksonMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isConflict())
			.andExpect(content().string(IsEmptyString.isEmptyString()));
	}
	
}
