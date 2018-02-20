package com.epam.asset.tracking.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.hamcrest.text.IsEmptyString;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.BusinessProviderService;
import com.epam.asset.tracking.web.AbstractWebTest;

import ma.glasnost.orika.MapperFacade;

public class EntityControllerTest extends AbstractWebTest{
	
	@MockBean
	ApiService api;
	
	@MockBean
	MapperFacade mapperFacade;
	
	@MockBean
	BusinessProviderService businessProviderService;
	
	@MockBean
	BusinessProviderRepository businessProviderRepository;
	
	private String username;
	
	@Before
	public void setup() {
		
		username = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("[0-9]", "");
		
	}

	@Test
	public void shouldReturn201() throws Exception {
		
		EntityDTO dto = new EntityDTO();
		dto.setAddress(
				"string with a comma, not at the end, of course, but with a period at the end. and one more thing...");
		dto.setBusinessType("businessType");
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
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(jacksonMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().string(IsEmptyString.isEmptyString()));
	}


}


