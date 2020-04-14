package com.epam.asset.tracking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Asset repository init failed :( -> Please contact the developers!")
public class BlockchainInitializationException extends RuntimeException {

  public BlockchainInitializationException(String message) {
    super(message);
  }

  public BlockchainInitializationException(Throwable cause) {
    super(cause);
  }

  public BlockchainInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
