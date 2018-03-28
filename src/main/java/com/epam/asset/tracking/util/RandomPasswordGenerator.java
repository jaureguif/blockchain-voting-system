package com.epam.asset.tracking.util;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RandomPasswordGenerator {

    public String generateNewPassword() {

        PasswordGenerator passwordGenerator = new PasswordGenerator();

        CharacterRule characterRule = new CharacterRule(EnglishCharacterData.Alphabetical);

        return passwordGenerator.generatePassword(8, characterRule);

    }

}
