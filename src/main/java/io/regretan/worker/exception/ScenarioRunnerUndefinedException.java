package io.regretan.worker.exception;

public class ScenarioRunnerUndefinedException extends RegretanWorkerException {

  public ScenarioRunnerUndefinedException(String message) {
    super(message);
  }

  public ScenarioRunnerUndefinedException(String message, Throwable cause) {
    super(message, cause);
  }
}
