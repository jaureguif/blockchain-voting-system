package com.epam.asset.tracking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Not able to Convert to Asset representation")
public class AssetConvertException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public AssetConvertException(String detailMessage) {
		super(detailMessage);
	}

	public AssetConvertException(String detailMessage, Throwable cause) {
		super(detailMessage, cause);
	}

}
