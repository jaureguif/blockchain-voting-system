package com.epam.asset.tracking.repository;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.epam.asset.tracking.Application;
import com.epam.asset.tracking.domain.Address;
import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.domain.BUSINESS_TYPE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class BusinessProviderRepositoryTest {
	
	@Autowired
	private BusinessProviderRepository repository;
	
	private static final Logger logger = LoggerFactory.getLogger(BusinessProviderRepositoryTest.class);
	
	private static final String CITY = "GDL";
	private static final String COUNTRY = "Mexico";
	private static final String STATE = "Jalisco";
	private static final String ZIPCODE = "3332";
	private static final String STREET = "Anillo periferico";
	private static final BUSINESS_TYPE BIZZ_TYPE = BUSINESS_TYPE.CAR_SELLER;
	private static final String EMAIL = "email@email.com";
	private static final String NAME = "audi factory";
	private static final String USERNAME = "audi";
	private static final String PASSWORD = "pass";
	private static final String TAX_ID = "ddas2332";

	@Test
	public void createNewBusinessProviderTest() {
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
		
		BusinessProvider result = repository.save(entity);
		assertNotNull(result);
		assertNotNull(result.getId());
		
		//Retrieve it from in-memory database to check that it was actually saved
		BusinessProvider businessProviderFromDb = repository.findByName(NAME);
		
		assertEquals(entity, businessProviderFromDb);
		logger.info("Retrieve it from in-memory database succeded");
		
	}

}
