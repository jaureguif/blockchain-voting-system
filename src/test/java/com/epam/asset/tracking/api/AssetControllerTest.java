package com.epam.asset.tracking.api;

import com.epam.asset.tracking.repository.BusinessProviderRepository;
import com.epam.asset.tracking.service.ApiService;
import com.epam.asset.tracking.service.BusinessProviderService;
import com.epam.asset.tracking.web.AbstractWebTest;
import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class AssetControllerTest extends AbstractWebTest {
    @MockBean
    ApiService api;

    @MockBean
    MapperFacade mapperFacade;

    @MockBean
    BusinessProviderService businessProviderService;

    @MockBean
    BusinessProviderRepository businessProviderRepository;

    @Test
    public void testConverter() throws Exception{
        mockMvc.perform(get("/asset/tracking/asset/9d40ee4e-bf1e-4f74-8237-c5e9b6e8f6d3"));

    }
}
