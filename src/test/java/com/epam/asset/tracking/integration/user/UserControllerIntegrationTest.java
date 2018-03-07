package com.epam.asset.tracking.integration.user;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.epam.asset.tracking.dto.UserDTO;
import com.epam.asset.tracking.integration.AbstractIntegrationTest;
import com.epam.asset.tracking.service.BusinessProviderService;
import org.springframework.security.test.context.support.WithMockUser;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

	String username;

	@SpyBean
	BusinessProviderService businessProviderService;

	@Before
	public void setup() {
		username = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("[0-9]", "");
	}

	@Test
	public void shouldReturn201() throws Exception {

		UserDTO dto = new UserDTO();
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

		mockMvc.perform(post("/asset/tracking/users/")
				.contentType(MediaType.APPLICATION_JSON).content(jacksonMapper.writeValueAsString(dto))).andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	public void shouldReturn409() throws Exception {

		UserDTO dto = new UserDTO();
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

		mockMvc.perform(post("/asset/tracking/users/")
				.contentType(MediaType.APPLICATION_JSON).content(jacksonMapper.writeValueAsString(dto))).andDo(print())
				.andExpect(status().isCreated());

		// Will insert a new DTO, but with same username
		dto = new UserDTO();
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

		doThrow(new DuplicateKeyException("Duplicated username")).when(businessProviderService).save(any());

		mockMvc.perform(post("/asset/tracking/users/")
				.contentType(MediaType.APPLICATION_JSON_UTF8).content(jacksonMapper.writeValueAsString(dto)))
				.andDo(print()).andExpect(status().isConflict())
				.andExpect(content().string(IsEmptyString.isEmptyString()));
	}


	@Test
	@WithMockUser(username = "admin", roles = {"BUSINESS_PROVIDER", "USER"})
	public void getUserData_Fail() {
		try {
			mockMvc.perform(get("/asset/tracking/users/mmonraz")
					.contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	@WithMockUser(username = "mmonraz", password = "0s0n3gr0",roles = {"BUSINESS_PROVIDER", "USER"})
	public void getUserData_Success() {
		try {
			mockMvc.perform(get("/asset/tracking/users/mmonraz")
					.contentType(MediaType.APPLICATION_JSON_UTF8))
					.andDo(print());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
