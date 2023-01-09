package io.regretan.scheduler;

import io.regretan.mapper.dto.RegretanInfoDto;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.TestScenarioDto;
import io.regretan.scheduler.repository.RegretanInfoRepository;
import io.regretan.scheduler.repository.TestScenarioRepository;
import io.regretan.worker.dto.QueueScenarioRequest;
import io.regretan.worker.dto.ResumeScenarioRequest;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "regretan.role", havingValue = "scheduler")
public class RegretanScheduler {

  private final @NonNull Clock clock;
  private final @NonNull RegretanInfoRepository regretanInfoRepository;
  private final @NonNull TestScenarioRepository testScenarioRepository;
  private final @NonNull RegretanWorkerClient regretanWorkerClient;
  private volatile LocalDateTime lastScheduled = LocalDateTime.MIN;

  @PostConstruct
  public void init() {
    final var regretanInfoOpt = regretanInfoRepository.find();
    lastScheduled = regretanInfoOpt
        .map(RegretanInfoDto::getLastScheduled)
        .orElseGet(() -> {
          // When the regret scheduler starts up initially, it assumes the last scheduled time is the current.
          final var initialStartUpTime = LocalDateTime.now(clock);
          regretanInfoRepository.updateLastScheduledTime(initialStartUpTime);
          return initialStartUpTime;
        });

    log.info("RegretanScheduler was initialized: lastScheduled={}", lastScheduled);
  }

  /**
   * This method is entry point for scheduling test scenario. Firstly, query registered test
   * scenarios, then evaluate whether it should be executed or not. If it should be, this scheduler
   * passes the scenario to a worker that isn't occupied, in order to run on it.
   */
  @Scheduled(fixedRate = 1000L)
  public synchronized void schedule() {
    try {
      final var scheduledTime = LocalDateTime.now(clock);

      scheduleToResumePendingScenarios(scheduledTime);
      newlyScheduleScenarios(scheduledTime);

      updateLastScheduledTime(scheduledTime);
    } catch (RuntimeException e) {
      log.error("Exception occurred in RegretanScheduler#schedule", e);
    }
  }

  private void scheduleToResumePendingScenarios(LocalDateTime scheduledTime) {
    final var scenariosToResume = testScenarioRepository.findScenariosToResume(scheduledTime);
    createResumeScenarioRequest(scenariosToResume)
        .forEach(regretanWorkerClient::resumeScenario);
  }

  private static List<ResumeScenarioRequest> createResumeScenarioRequest(
      Collection<ScenarioExecutionDto> scenarioExecutions
  ) {
    return scenarioExecutions
        .stream()
        .map(execution -> ResumeScenarioRequest
            .builder()
            .executionId(execution.getExecutionId())
            .nextStep(execution.getNextStep())
            .build())
        .toList();
  }

  private void newlyScheduleScenarios(LocalDateTime scheduledTime) {
    final var activeScenarios = testScenarioRepository.findActiveScenarios();
    final var newlyScheduledScenarios = filterShouldBeScheduled(scheduledTime, activeScenarios);

    newlyScheduledScenarios.forEach(regretanWorkerClient::queueNewScenario);
  }

  private void updateLastScheduledTime(LocalDateTime scheduledTime) {
    regretanInfoRepository.updateLastScheduledTime(scheduledTime);
    lastScheduled = scheduledTime;
  }

  private List<QueueScenarioRequest> filterShouldBeScheduled(
      LocalDateTime scheduledTime, Collection<TestScenarioDto> activeScenarios
  ) {
    return activeScenarios
        .stream()
        .filter(scenario -> {
          final var nextScheduleTime = scenario.getNextScheduleAfter(lastScheduled);
          return !nextScheduleTime.isAfter(scheduledTime);
        })
        .map(scenario -> QueueScenarioRequest
            .builder()
            .namespace(scenario.getNamespace())
            .scenarioName(scenario.getScenarioName())
            .generation(scenario.getGeneration())
            .scheduledTime(scenario
                .getNextScheduleAfter(lastScheduled)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .build())
        .toList();
  }

}
