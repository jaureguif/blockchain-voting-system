package com.epam.asset.tracking.dto.validations;

public interface ValidationsUtil {
	
	String LETTER = "[a-zA-ZáéíóúÁÉĪÓÚ\\u00f1\\u00d1]";
	
	String LETTER_OR_NUMBER = "[a-zA-Z0-9áéíóúÁÉĪÓÚ\\u00f1\\u00d1]";
	
	

	String LETTERS_ONLY = "^" + LETTER + "*$";
	
	String LETTERS_WITH_SPACE = "^(" + LETTER +"+[\\s]?)*" + LETTER + "$";
	
	String LETTERS_AND_NUMBERS = "^" + LETTER_OR_NUMBER + "*$";
	
	String LETTERS_AND_NUMBERS_WITH_SPACE = "^(" + LETTER_OR_NUMBER +"+[\\s]?)*" + LETTER_OR_NUMBER + "$";
	
	
}
