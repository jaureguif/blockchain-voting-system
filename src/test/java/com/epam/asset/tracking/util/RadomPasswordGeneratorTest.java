package com.epam.asset.tracking.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RadomPasswordGeneratorTest {

    @Autowired
    private RandomPasswordGenerator randomPasswordGenerator;

    @Test
    public void shouldGenerateRandomPassowrd(){
        randomPasswordGenerator = new RandomPasswordGenerator();
        String firstPassword = randomPasswordGenerator.generateNewPassword();
        String secondPassword = randomPasswordGenerator.generateNewPassword();

        Assert.assertNotEquals(firstPassword, secondPassword);
    }
}
