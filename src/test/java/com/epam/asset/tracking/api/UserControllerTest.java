package com.epam.asset.tracking.api;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.User.Role;
import com.epam.asset.tracking.dto.UserDTO;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.BusinessProviderService;
import com.epam.asset.tracking.web.AbstractWebTest;
import ma.glasnost.orika.MapperFacade;
import org.hamcrest.text.IsEmptyString;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

public class UserControllerTest extends AbstractWebTest{
	
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
	@Ignore
	public void shouldReturn201() throws Exception {
		
		UserDTO dto = new UserDTO();
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
		dto.setRole(Role.BUSINESS_PROVIDER.name()); //to get mock mapping match
		System.out.println("USERNAME: " + username);
		dto.setUsername(username);
		
		System.out.println(jacksonMapper.writeValueAsString(dto));
		
		mockMvc.perform(post("/asset/tracking/users/")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(jacksonMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isCreated())
			.andExpect(content().string(IsEmptyString.isEmptyString()));
	}

	
	@Test
	@Ignore
	public void shouldReturn409() throws Exception {
		
		UserDTO dto = new UserDTO();
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
		
		BusinessProvider bp = new BusinessProvider();
		bp.setUsername(username);
		
		System.out.println(jacksonMapper.writeValueAsString(dto));
		doReturn(bp)
			.when(mapperFacade).map(refEq(dto, "role"), eq(BusinessProvider.class));
		
		when(businessProviderService.save(bp)).thenThrow(new DuplicateKeyException("Duplicated username"));
		
		mockMvc.perform(post("/asset/tracking/users/")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(jacksonMapper.writeValueAsString(dto)))
			.andDo(print())
			.andExpect(status().isConflict())
			.andExpect(content().string(IsEmptyString.isEmptyString()));
	}

	@Test
	@WithMockUser(username = "useraNLH", password = "qwerty1234",roles = {"BUSINESS_PROVIDER", "USER"})
	public void getUserData() {
		try {
			mockMvc.perform(post("/asset/tracking/users/mmonraz")
                    .contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(print());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}


