package io.regretan.worker.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QueueScenarioRequest {

  String namespace;
  String scenarioName;
  long generation;
  String scheduledTime;
}
