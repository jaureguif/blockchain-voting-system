package com.epam.asset.tracking.domain;

import org.springframework.util.StringUtils;

import java.util.Locale;

public enum BUSINESS_TYPE {
	COMPUTER_SELLER, COMPUTER_REPAIR, CAR_SELLER, CAR_MECHANIC, HOUSE_SELLER, HOUSE_BROKER;

	public static BUSINESS_TYPE getEnum(String name) {

		String converted = name.toUpperCase(Locale.ENGLISH).trim().replaceAll(" ", "_");

		BUSINESS_TYPE bt = null;
		try {
			bt = BUSINESS_TYPE.valueOf(converted);
		} catch (IllegalArgumentException iae) {
			BUSINESS_TYPE.valueOf(name);
		}
		return bt;

	}

	public static String getValue(BUSINESS_TYPE enumValue) {

		String value = StringUtils.capitalize(enumValue.name().trim().replaceAll("_", " ").toLowerCase(Locale.ENGLISH));
		return value;

	}

}