package io.regretan.worker.repository;

import io.regretan.mapper.TestScenarioMapper;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.TestScenarioDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WorkerTestScenarioRepository {

  private final @NonNull TestScenarioMapper testScenarioMapper;

  public Optional<TestScenarioDto> find(
      String namespace, String scenarioName, long generation
  ) {
    return Optional.ofNullable(
        testScenarioMapper.findScenario(namespace, scenarioName, generation, true));
  }

  public ScenarioExecutionDto findExecution(long scenarioId) {
    return testScenarioMapper.findExecutionById(scenarioId, true);
  }

  public List<ScenarioExecutionDto> findExclusiveExecutions(
      String namespace, String scenarioName, String scheduledTime
  ) {
    return testScenarioMapper.selectExclusiveExecutions(namespace, scenarioName, scheduledTime);
  }

  public ScenarioExecutionDto createNewExecution(ScenarioExecutionDto scenarioExecutionDto) {
    testScenarioMapper.insertScenarioExecution(scenarioExecutionDto);
    return testScenarioMapper.findExecutionById(scenarioExecutionDto.getExecutionId(), false);
  }

  public void pendExecution(long executionId, int nextStep, LocalDateTime nextScheduleTime) {
    testScenarioMapper.pendExecution(executionId, nextStep, nextScheduleTime);
  }

  public void resumeExecution(long executionId) {
    testScenarioMapper.resumeExecution(executionId);
  }

  public void failedExecution(long executionId, String failedReason) {
    testScenarioMapper.failedExecution(executionId, failedReason);
  }

  public void finishExecution(long executionId) {
    testScenarioMapper.finishExecution(executionId);
  }

  public void updateStep(long executionId, int nextStep) {
    testScenarioMapper.updateStep(executionId, nextStep);
  }

}
