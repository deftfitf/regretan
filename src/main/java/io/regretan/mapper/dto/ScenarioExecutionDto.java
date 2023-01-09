package io.regretan.mapper.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ScenarioExecutionDto {

  private long executionId;
  private String namespace;
  private String scenarioName;
  private long generation;
  private int nextStep;
  private ExecutionStatus status;
  private String failedReason;
  private String scheduledTime;
  private LocalDateTime startTime;
  private LocalDateTime nextStepTime;
  private LocalDateTime endTime;

  public enum ExecutionStatus {
    RUNNING,
    PENDING,
    FINISHED,
    FAILED,
    ;
  }

}
