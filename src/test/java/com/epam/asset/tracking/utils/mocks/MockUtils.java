package com.epam.asset.tracking.utils.mocks;

import com.epam.asset.tracking.domain.Address;
import com.epam.asset.tracking.domain.BusinessProvider;
import com.epam.asset.tracking.dto.EntityDTO;

/**
 * Created  on 1/26/2018.
 */
public class MockUtils {
    public static EntityDTO mockUser() {
        EntityDTO dto = new EntityDTO();

        dto.setAddress("5th st ");
        dto.setName("ddd");
        dto.setRfc("jomd123456");
        dto.setCity("NY");


        dto.setZipCode("123456");
        dto.setUserName("d");
        dto.setZipCode("12345");
        dto.setLastName("jjj");
        dto.setBusinessType("btype");
        dto.setMail("d@epam.com");
        dto.setPassword("admin");

        return dto;
    }
}
