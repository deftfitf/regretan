package io.regretan.mapper.dto;

import io.regretan.worker.exception.RegretanWorkerException;

public class ResumeScenarioNotFoundException extends RegretanWorkerException {

  public ResumeScenarioNotFoundException(String message) {
    super(message);
  }

  public ResumeScenarioNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
