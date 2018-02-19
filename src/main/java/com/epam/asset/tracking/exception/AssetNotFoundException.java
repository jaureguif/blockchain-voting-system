package com.epam.asset.tracking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Asset not found")
public class AssetNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AssetNotFoundException(String detailMessage) {
		super(detailMessage);
	}

	public AssetNotFoundException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}

}
