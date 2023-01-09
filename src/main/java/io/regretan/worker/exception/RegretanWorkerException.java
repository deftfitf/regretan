package io.regretan.worker.exception;

public class RegretanWorkerException extends RuntimeException {

  public RegretanWorkerException(String message) {
    super(message);
  }

  public RegretanWorkerException(String message, Throwable cause) {
    super(message, cause);
  }

}
