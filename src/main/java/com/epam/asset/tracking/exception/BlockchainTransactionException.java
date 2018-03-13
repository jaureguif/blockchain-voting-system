package com.epam.asset.tracking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Transaction failed :( -> Please contact the developers!")
public class BlockchainTransactionException extends RuntimeException {

  public BlockchainTransactionException(String message) {
    super(message);
  }

  public BlockchainTransactionException(Throwable cause) {
    super(cause);
  }

  public BlockchainTransactionException(String message, Throwable cause) {
    super(message, cause);
  }
}
