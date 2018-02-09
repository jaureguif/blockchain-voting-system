package com.epam.asset.tracking.utils.mocks;

import com.epam.asset.tracking.dto.EntityDTO;

/**
 * Created on 1/26/2018.
 */
public class MockUtils {
	public static EntityDTO mockUser() {
		EntityDTO dto = new EntityDTO();

		dto.setAddress("5th st ");
		dto.setName("ddd");
		dto.setRfc("jomd123456");
		dto.setCity("NY");

		dto.setZipCode("123456");
		dto.setUsername("d");
		dto.setZipCode("12345");
		dto.setLastName("jjj");
		dto.setBusinessType("car seller");
		dto.setEmail("d@epam.com");
		dto.setPassword("admin");
		dto.setRole("BUSINESS_PROVIDER");

		return dto;
	}
}
