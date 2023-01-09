package io.regretan.worker;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.regretan.customresource.dsl.Matcher;
import io.regretan.customresource.dsl.Matcher.MatchResult;
import io.regretan.customresource.dsl.TestAction;
import io.regretan.mapper.dto.ResumeScenarioNotFoundException;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.ScenarioExecutionDto.ExecutionStatus;
import io.regretan.mapper.dto.TestScenarioDto;
import io.regretan.setting.RegretanSetting;
import io.regretan.worker.exception.DuplicatedExecutionException;
import io.regretan.worker.exception.RegretanWorkerException;
import io.regretan.worker.exception.ScenarioNotFoundException;
import io.regretan.worker.exception.ScenarioRunnerUndefinedException;
import io.regretan.worker.repository.WorkerTestScenarioRepository;
import io.regretan.worker.scenario.ScenarioRunner;
import io.regretan.worker.scenario.ScenarioRunnerRegistry;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(name = "regretan.role", havingValue = "worker")
public class RegretanWorker {

  private static final String REGRETAN_WORKER_EXECUTOR_NAME = "regretan-worker";
  private static final int REGRETAN_WORKER_EXECUTOR_AWAIT_TERMINATION_SEC = 60;

  private final @NonNull Clock clock;
  private final @NonNull MeterRegistry meterRegistry;
  private final @NonNull WorkerTestScenarioRepository workerTestScenarioRepository;
  private final @NonNull ScenarioRunnerRegistry scenarioRunnerRegistry;
  private final @NonNull ExecutorService regretanWorkerExecutor;

  public RegretanWorker(
      @NonNull Clock clock,
      @NonNull RegretanSetting regretanSetting,
      @NonNull MeterRegistry meterRegistry,
      @NonNull WorkerTestScenarioRepository workerTestScenarioRepository,
      @NonNull ScenarioRunnerRegistry scenarioRunnerRegistry) {
    this.clock = clock;
    this.meterRegistry = meterRegistry;
    this.workerTestScenarioRepository = workerTestScenarioRepository;
    this.scenarioRunnerRegistry = scenarioRunnerRegistry;
    regretanWorkerExecutor =
        Executors.newFixedThreadPool(
            regretanSetting.getWorker().getConcurrency(),
            new NamedThreadFactory(REGRETAN_WORKER_EXECUTOR_NAME));
  }

  @PostConstruct
  public void init() {
    ExecutorServiceMetrics.monitor(
        meterRegistry, regretanWorkerExecutor, REGRETAN_WORKER_EXECUTOR_NAME);
  }

  @PreDestroy
  public void cleanup() throws InterruptedException {
    regretanWorkerExecutor.shutdown();
    if (!regretanWorkerExecutor.awaitTermination(
        REGRETAN_WORKER_EXECUTOR_AWAIT_TERMINATION_SEC,
        TimeUnit.SECONDS)) {
      log.error("Failed to graceful shutdown RegretanWorker.");
    }
  }

  @Transactional
  public void runScenario(
      String namespace, String scenarioName, long generation, String scheduledTime
  ) throws RegretanWorkerException {
    checkExclusion(namespace, scenarioName, generation, scheduledTime);

    final var scenario = getScenario(namespace, scenarioName, generation);
    if (scenario.isDeleted()) {
      log.info("The execution will be skipped because the scenario has already been deleted. "
              + "namespace={}, scenarioName={}, generation={}",
          scenario.getNamespace(), scenario.getScenarioName(), scenario.getGeneration());
      return;
    }

    final var execution = createExecution(namespace, scenarioName, generation, scheduledTime);
    final var startStep = 1;
    regretanWorkerExecutor.submit(new RegretanWorkerRunner(startStep, scenario, execution));
  }

  private TestScenarioDto getScenario(String namespace, String scenarioName, long generation)
      throws ScenarioNotFoundException {
    return workerTestScenarioRepository
        .find(namespace, scenarioName, generation)
        .orElseThrow(() -> new ScenarioNotFoundException(
            String.format("The scenario to run isn't there: "
                    + "namespace=%s, scenarioName=%s, generation=%s",
                namespace, scenarioName, generation)));
  }

  private ScenarioExecutionDto createExecution(
      String namespace, String scenarioName, long generation, String scheduledTime
  ) {
    final var executionDto = new ScenarioExecutionDto();
    executionDto.setNamespace(namespace);
    executionDto.setScenarioName(scenarioName);
    executionDto.setGeneration(generation);
    executionDto.setScheduledTime(scheduledTime);
    executionDto.setStatus(ExecutionStatus.RUNNING);
    executionDto.setNextStep(1);
    return workerTestScenarioRepository.createNewExecution(executionDto);
  }

  private void checkExclusion(
      String namespace, String scenarioName, long generation, String scheduledTime
  ) throws DuplicatedExecutionException {
    final var exclusiveExecutions = workerTestScenarioRepository
        .findExclusiveExecutions(namespace, scenarioName, scheduledTime);
    if (!exclusiveExecutions.isEmpty()) {
      final var exclusiveIds = exclusiveExecutions
          .stream()
          .map(ScenarioExecutionDto::getExecutionId)
          .map(String::valueOf)
          .collect(Collectors.joining(","));
      throw new DuplicatedExecutionException(
          String.format("The execution was skipped to run "
                  + "becausse there is at least one execution that should be exclusive.: "
                  + "namespace=%s, scenarioName=%s, generation=%s, exclusiveExecutionIds=%s",
              namespace, scenarioName, generation, exclusiveIds));
    }
  }

  @Transactional
  public void resumeScenario(long scenarioId, int nextStep) throws RegretanWorkerException {
    final var execution = findExecution(scenarioId, nextStep);
    final var scenario = getScenario(
        execution.getNamespace(), execution.getScenarioName(), execution.getGeneration());

    workerTestScenarioRepository.resumeExecution(scenarioId);
    regretanWorkerExecutor.submit(new RegretanWorkerRunner(
        // If the scenario has already been deleted, execute cleanup process directly.
        scenario.isDeleted() && scenario.isDefinedCleanup()
            ? scenario.getCleanupStep()
            : scenario.getStepNum(),
        scenario, execution));
  }

  private ScenarioExecutionDto findExecution(long scenarioId, int nextStep) {
    final var execution = workerTestScenarioRepository.findExecution(scenarioId);
    if (execution == null ||
        execution.getStatus() != ExecutionStatus.PENDING ||
        execution.getNextStep() != nextStep) {
      throw new ResumeScenarioNotFoundException(
          String.format(
              "Pending scenario to resume is not found. scenarioId=%s, nextStep=%s",
              scenarioId, nextStep));
    }
    return execution;
  }

  @RequiredArgsConstructor
  public class RegretanWorkerRunner implements Runnable {

    final int startStep;
    final TestScenarioDto scenario;
    final ScenarioExecutionDto execution;

    @Override
    public void run() {
      try {
        runMain();
      } catch (RuntimeException e) {
        log.error(
            "Unhandled exception was occurred at RegretanWorkerRunner: "
                + "startStep={}, executionId={}", scenario, execution.getExecutionId(), e);
      }
    }

    public void runMain() {
      final var stepNum = scenario.getStepNum();
      int nextStepN = startStep;
      while (nextStepN <= stepNum) {
        final var nextStep = scenario.getStepOf(nextStepN);
        final var nextAction = nextStep.getAction();

        // check that the delay field is defined on the nextStep
        if (nextStepN > startStep && nextAction.getDelay() != null) {
          final var resumeTime = LocalDateTime.now(clock)
              .plus(nextAction.getDelay());
          workerTestScenarioRepository.pendExecution(
              execution.getExecutionId(), nextStepN, resumeTime);
          return;
        }

        final var variables = runTestAction(nextStepN, nextAction);
        if (variables == null) {
          // If failed to execute scenario, clean up immediately.
          runCleanupIfDefined();
          return;
        }

        // test matcher
        final var matchers = nextStep.getMatchers();
        final var matchResult = assertWithMatchers(matchers, variables);
        if (!matchResult.success()) {
          runCleanupIfDefined();
          workerTestScenarioRepository.failedExecution(
              execution.getExecutionId(),
              matchResult.failedReason());
          return;
        }

        workerTestScenarioRepository.updateStep(execution.getExecutionId(), ++nextStepN);
      }

      runCleanupIfDefined();
      workerTestScenarioRepository.finishExecution(execution.getExecutionId());
    }

    private void runCleanupIfDefined() {
      if (scenario.isDefinedCleanup()) {
        runTestAction(scenario.getCleanupStep(), scenario.getCleanupAction());
      }
    }

    private Map<String, String> runTestAction(int nextStepN, TestAction nextAction) {
      // pick up a runner to execute scenario by kind field in TestStep.
      final ScenarioRunner<?> runner;
      try {
        runner = scenarioRunnerRegistry.getScenarioRunner(nextAction.getKind());
      } catch (ScenarioRunnerUndefinedException e) {
        log.error(
            "Fatal Error occurred: namespace={}, scenarioName={}, generation={}, step={}, kind={}",
            scenario.getNamespace(), scenario.getScenarioName(), scenario.getGeneration(),
            nextStepN, nextAction.getKind(), e);
        workerTestScenarioRepository.failedExecution(execution.getExecutionId(), e.getMessage());
        return null;
      }

      // run scenario runner with contexts.

      final Map<String, String> variables;
      try {
        @SuppressWarnings("unchecked") final var obj = (Map<String, String>) runner
            .getClass()
            .getDeclaredMethod(
                "run", TestScenarioDto.class, ScenarioExecutionDto.class,
                int.class, runner.testActionClass())
            .invoke(runner, scenario, execution, nextStepN, nextAction);
        log.debug("ScenarioRunner was finished successfully: variables={}", obj);
        variables = obj;
      } catch (Exception e) {
        log.info(
            "Failed to run scenario: namespace={}, scenarioName={}, generation={}, step={}, kind={}",
            scenario.getNamespace(), scenario.getScenarioName(), scenario.getGeneration(),
            nextStepN, nextAction.getKind(), e);
        workerTestScenarioRepository.failedExecution(
            execution.getExecutionId(),
            String.format("Failed to run scenario: %s", e.getMessage()));
        return null;
      }

      return variables;
    }

    private MatchResult assertWithMatchers(
        List<Matcher> matchers, Map<String, String> variables
    ) {
      return matchers
          .stream()
          .map(matcher -> matcher.matches(variables))
          .filter(Predicate.not(MatchResult::success))
          .findFirst()
          .orElse(new MatchResult(true, null));
    }

  }

}
