package io.regretan.worker.dto;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeScenarioRequest {

  long executionId;
  int nextStep;
}
