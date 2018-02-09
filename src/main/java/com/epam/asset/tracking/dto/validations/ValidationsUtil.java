package com.epam.asset.tracking.dto.validations;

public final class ValidationsUtil {
	
	private static final String LETTER = "[a-zA-ZáéíóúÁÉĪÓÚ\\u00f1\\u00d1]";
	
	private static final String LETTER_OR_NUMBER = "[a-zA-Z0-9áéíóúÁÉĪÓÚ\\u00f1\\u00d1]";
	
	public static final String LETTERS_ONLY = "^" + LETTER + "*$";
	
	public static final String LETTERS_WITH_SPACE = "^(" + LETTER +"+[\\s]?)*" + LETTER + "$";
	
	public static final String LETTERS_AND_NUMBERS = "^" + LETTER_OR_NUMBER + "*$";
	
	public static final String LETTERS_AND_NUMBERS_WITH_SPACE = "^(" + LETTER_OR_NUMBER +"+[\\s]?)*" + LETTER_OR_NUMBER + "$";
	
	public static final String LETTERS_AND_NUMBERS_WITH_SPACE_COMMA_AND_PERIOD = "^(" + LETTER_OR_NUMBER + "+[,|[.]{0,3}]?[\\s]?)*" + LETTER_OR_NUMBER + "+[.]{0,3}$";
		
}
