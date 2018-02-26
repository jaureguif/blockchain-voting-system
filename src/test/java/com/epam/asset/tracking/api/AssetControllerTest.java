package com.epam.asset.tracking.api;

import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.BusinessProviderService;
import com.epam.asset.tracking.web.AbstractWebTest;
import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class AssetControllerTest extends AbstractWebTest {
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
