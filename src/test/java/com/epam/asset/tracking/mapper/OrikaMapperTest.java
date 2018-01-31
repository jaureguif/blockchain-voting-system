package com.epam.asset.tracking.mapper;

/**
 * Created  on 1/26/2018.
 */

import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.dto.EntityDTO;
import com.epam.asset.tracking.mapper.custom.EntityDtoToBusinessProviderMapper;
import com.epam.asset.tracking.utils.mocks.MockUtils;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class OrikaMapperTest {

	@Test
	public void shouldBeMapped() {

		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().mapNulls(false).build();
		EntityDtoToBusinessProviderMapper custom = new EntityDtoToBusinessProviderMapper();
		mapperFactory.classMap(EntityDTO.class, BusinessProvider.class).customize(custom).byDefault().register();
		MapperFacade mapper = mapperFactory.getMapperFacade();

		EntityDTO dto = MockUtils.mockUser();

		Assert.assertNotNull(dto.getAddress());
		Assert.assertNotNull(dto);

		BusinessProvider provider = mapper.map(dto, BusinessProvider.class);

		Assert.assertNotNull(provider);
		Assert.assertNotNull(provider.getEmail());
		Assert.assertThat(provider.getEmail(), Matchers.is(dto.getEmail()));
		Assert.assertNotNull(provider.getAddress());
		Assert.assertNotNull(provider.getAddress().getCity());
	}

}
