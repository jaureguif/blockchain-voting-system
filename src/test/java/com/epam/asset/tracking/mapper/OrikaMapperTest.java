package com.epam.asset.tracking.mapper;

/**
 * Created  on 1/26/2018.
 */


import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.utils.mocks.MockUtils;
import ma.glasnost.orika.MapperFacade;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest


public class OrikaMapperTest {

    @Autowired
    private MapperFacade mapper;

    @Test
    public void shouldBeMapped() {

        EntityDTO dto = MockUtils.mockUser();

        Assert.assertNotNull(dto.getAddress());
        Assert.assertNotNull(dto);

        BusinessProvider user = mapper.map(dto, BusinessProvider.class);

        Assert.assertNotNull(user);
        Assert.assertNotNull(user.getEmail());
        Assert.assertNotNull(user.getAddress());
        Assert.assertNotNull(user.getAddress().getCity());
    }

}

