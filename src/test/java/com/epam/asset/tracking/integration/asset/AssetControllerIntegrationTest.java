package com.epam.asset.tracking.integration.asset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.asset.tracking.dto.EntityDTO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import com.epam.asset.tracking.integration.AbstractIntegrationTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

public class AssetControllerIntegrationTest extends AbstractIntegrationTest {

	private String username;

	@Before
	public void setup() {

		username = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("[0-9]", "");

	}

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

	@Test
	@WithMockUser(username = "admin", roles = {"BUSINESS_PROVIDER", "USER"})
	public void testConverter() throws Exception{

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

		
		
		MockMultipartFile multipartFile = new MockMultipartFile("file","FileUploadTest.txt",null, new byte[100]);

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/asset/tracking/asset/")
				.file(multipartFile)
				.content(jacksonMapper.writeValueAsString(dto)))
				.andDo(print())
				.andExpect(status().is2xxSuccessful());


	}
	
	
}
