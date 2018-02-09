package com.epam.asset.tracking.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Username already taken")
public class DuplicateKeyExceptionWrapper extends DuplicateKeyException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateKeyExceptionWrapper(String detailMessage) {
		super(detailMessage);
	}

	public DuplicateKeyExceptionWrapper(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}

}
