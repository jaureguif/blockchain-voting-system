package com.epam.asset.tracking.util;

import org.bouncycastle.crypto.prng.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

import static org.mockito.Mockito.mock;

public class RadomPasswordGeneratorTest {

    private RandomPasswordGenerator randomPasswordGenerator;

    @Test
    public void shouldGenerateRandomPassowrd(){
        randomPasswordGenerator = new RandomPasswordGenerator();
        String firstPassword = randomPasswordGenerator.generateNewPassword();
        String secondPassword = randomPasswordGenerator.generateNewPassword();

        Assert.assertNotEquals(firstPassword, secondPassword);
    }
}
