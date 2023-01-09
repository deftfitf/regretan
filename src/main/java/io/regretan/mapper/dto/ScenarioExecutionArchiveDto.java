package io.regretan.mapper.dto;

import io.regretan.mapper.dto.ScenarioExecutionDto.ExecutionStatus;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class ScenarioExecutionArchiveDto {

  long archiveId;
  long executionId;
  String namespace;
  String scenarioName;
  long generation;
  ExecutionStatus status;
  String scheduledTime;
  LocalDateTime startTime;
  LocalDateTime endtime;
}
