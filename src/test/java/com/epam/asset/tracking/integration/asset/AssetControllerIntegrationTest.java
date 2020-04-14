package com.epam.asset.tracking.integration.asset;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.epam.asset.tracking.domain.Address;
import com.epam.asset.tracking.domain.Asset;
import com.epam.asset.tracking.domain.BUSINESS_TYPE;
import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.Event;
import com.epam.asset.tracking.exception.AssetNotFoundException;
import com.epam.asset.tracking.integration.AbstractIntegrationTest;
import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.service.ApiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

public class AssetControllerIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private BusinessProviderRepository businesProviderRepository;

  @MockBean
  ApiService api;

  private static String username;

  private static final String CITY = "GDL";
  private static final String COUNTRY = "Mexico";
  private static final String STATE = "Jalisco";
  private static final String ZIPCODE = "3332";
  private static final String STREET = "Anillo periferico";
  private static final BUSINESS_TYPE BIZZ_TYPE = BUSINESS_TYPE.CAR_SELLER;
  private static final String EMAIL = "email@email.com";
  private static final String NAME = "audi factory";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "pass";
  private static final String TAX_ID = "ddas2332";

  @Before
  public void setup() {
    username = UUID.randomUUID().toString().replaceAll("-", "").replaceAll("[0-9]", "");
    businesProviderRepository.save(getBusinessProviderEntity());
  }

  private BusinessProvider getBusinessProviderEntity() {
    BusinessProvider entity = new BusinessProvider();
    Address address = new Address();
    address.setCity(CITY);
    address.setCountry(COUNTRY);
    address.setState(STATE);
    address.setZipCode(ZIPCODE);
    address.setStreet(STREET);
    entity.setAddress(address);
    entity.setType(BIZZ_TYPE);
    entity.setEmail(EMAIL);
    entity.setName(NAME);
    entity.setUsername(USERNAME);
    entity.setPassword(PASSWORD);
    entity.setRfc(TAX_ID);
    return entity;
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
    when(api.getAssetById(UUID.fromString("9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d2")))
        .thenThrow(new AssetNotFoundException(""));
    mockMvc.perform(get("/asset/tracking/asset/9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d2")
        .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isNotFound());

  }

  @Test
  @WithMockUser(username = "admin", roles = {"BUSINESS_PROVIDER", "USER"})
  public void testConverter() throws Exception {
    MockMultipartFile multipartFile = new MockMultipartFile("file", "FileUploadTest.txt", null, new byte[100]);
    mockMvc.perform(fileUpload("/asset/tracking/asset/").file(multipartFile)
                                                        .param("assetType", "BUSINESS type")
                                                        .param("description", "A brief description")
                                                        .param("ownerName", "Owner 1")
                                                        .param("serialNumber", "123456789")
                                                        .param("summary",
                                                            "A summary for this asset")
                                                        .accept(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().is2xxSuccessful());


  }

  @Test
  @WithMockUser(username = "admin", roles = {"BUSINESS_PROVIDER", "USER"})
  public void shouldNewAssetEventReturn200WhenEventIsValid() throws Exception {
    Event event = new Event();
    event.setDate(ZonedDateTime.now());
    event.setBusinessProviderId("BUSINESS_PROVIDER");
    event.setEncodedImage("image");
    event.setAttachment("no-data");
    event.setDescription("A brief description");
    event.setSummary("A summary for this asset");
    Asset asset = new Asset();
    asset.setUuid(UUID.fromString("9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d3"));
    asset.getEvents().add(event);
    when(api.addEventToAsset(eq(asset.getUuid()), any(Event.class))).thenReturn(asset);

    MockMultipartFile multipartFile = new MockMultipartFile("image", "FileUploadTest.txt", null, new byte[100]);
    mockMvc
        .perform(fileUpload("/asset/tracking/asset/" + asset.getUuid() + "/event")
            .file(multipartFile)
            .param("description", event.getDescription())
            .param("summary", event.getSummary())
            .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(username = "admin", roles = {"BUSINESS_PROVIDER", "USER"})
  public void shouldNewAssetEventReturn400WhenEventIsNotValid() throws Exception {
    MockMultipartFile multipartFile = new MockMultipartFile("image", "FileUploadTest.txt", null, new byte[100]);
    mockMvc
        .perform(fileUpload("/asset/tracking/asset/9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d3/event")
            .file(multipartFile)
            .param("description", "")
            .param("summary", "")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "admin", roles = {"BUSINESS_PROVIDER", "USER"})
  public void shouldNewAssetEventReturn404WhenAssetIsNotFound() throws Exception {
    when(api.addEventToAsset(any(UUID.class), any(Event.class))).thenThrow(new AssetNotFoundException("Asset not found with the given id"));
    MockMultipartFile multipartFile = new MockMultipartFile("image", "FileUploadTest.txt", null, new byte[100]);
    mockMvc
        .perform(fileUpload("/asset/tracking/asset/9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d3/event")
            .file(multipartFile)
            .param("description", "A brief description")
            .param("summary", "A summary for this asset")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}
