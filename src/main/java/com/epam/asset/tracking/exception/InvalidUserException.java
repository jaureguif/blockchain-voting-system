package com.epam.asset.tracking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason = "Invalid username provided")
public class InvalidUserException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidUserException(String detailMessage) {
		super(detailMessage);
	}

	public InvalidUserException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}

}
