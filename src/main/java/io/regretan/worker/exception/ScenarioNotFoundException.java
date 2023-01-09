package io.regretan.worker.exception;

public class ScenarioNotFoundException extends RegretanWorkerException {

  public ScenarioNotFoundException(String message) {
    super(message);
  }

  public ScenarioNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
