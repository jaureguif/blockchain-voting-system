package com.epam.asset.tracking.utils.mocks;

import com.epam.asset.tracking.entities.AnEntity;

/**
 * Created  on 1/26/2018.
 */
public class MockUtils {
    public static AnEntity mockUser() {
        AnEntity entity = new AnEntity();

        entity.setAddress(Math.random()+ "");
        entity.setBusinessType(Math.random()+ "");
        entity.setCity(Math.random()+ "");
        entity.setLastName(Math.random()+ "");
        entity.setName(Math.random()+ "");
        entity.setRfc(Math.random()+ "");
        entity.setUserName(Math.random()+ "");
        entity.setZipCode(Math.random()+ "");

        return entity;
    }
}
