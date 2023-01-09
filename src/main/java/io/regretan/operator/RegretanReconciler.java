package io.regretan.operator;

import io.javaoperatorsdk.operator.api.reconciler.Cleaner;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusHandler;
import io.javaoperatorsdk.operator.api.reconciler.ErrorStatusUpdateControl;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.regretan.customresource.TestScenario;
import io.regretan.customresource.TestScenarioStatus;
import io.regretan.mapper.dto.TestScenarioDto;
import io.regretan.mapper.dto.TestScenarioDto.ScenarioDto;
import io.regretan.scheduler.repository.TestScenarioRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ControllerConfiguration
@RequiredArgsConstructor
public class RegretanReconciler implements
    Reconciler<TestScenario>,
    Cleaner<TestScenario>,
    ErrorStatusHandler<TestScenario> {

  private final TestScenarioRepository testScenarioRepository;

  @Override
  @Transactional
  public UpdateControl<TestScenario> reconcile(
      TestScenario resource, Context<TestScenario> context
  ) {
    log.info("Started reconcile: {}", resource);

    final var testScenario = createTestScenarioDto(resource);
    testScenarioRepository.create(testScenario);

    patchSyncedTestScenario(resource);

    return UpdateControl.patchStatus(resource);
  }

  private void patchSyncedTestScenario(TestScenario resource) {
    if (resource.getStatus() == null) {
      resource.setStatus(new TestScenarioStatus());
    }

    resource.getStatus().setSynced(true);
    resource.getStatus().setErrorMessage(null);
  }

  private static TestScenarioDto createTestScenarioDto(TestScenario resource) {
    final var testScenarioDto = new TestScenarioDto();
    testScenarioDto.setNamespace(resource.getMetadata().getNamespace());
    testScenarioDto.setScenarioName(resource.getMetadata().getName());
    testScenarioDto.setGeneration(resource.getMetadata().getGeneration());

    final var scenarioDto = new ScenarioDto();
    scenarioDto.setSchedule(resource.getSpec().getSchedule());
    scenarioDto.setTestSteps(resource.getSpec().getSteps());
    testScenarioDto.setScenario(scenarioDto);

    return testScenarioDto;
  }

  @Override
  @Transactional
  public DeleteControl cleanup(TestScenario resource, Context<TestScenario> context) {
    final var testScenario = createTestScenarioDto(resource);
    testScenarioRepository.markAsDelete(testScenario);

    final var remainingExecutions = testScenarioRepository
        .findRemainingExecutions(testScenario);
    if (!remainingExecutions.isEmpty()) {
      testScenarioRepository.reschedulePendingJobImmediately(testScenario);
      return DeleteControl
          .noFinalizerRemoval()
          .rescheduleAfter(Duration.ofSeconds(5));
    }

    testScenarioRepository.delete(testScenario);
    return DeleteControl.defaultDelete();
  }

  @Override
  public ErrorStatusUpdateControl<TestScenario> updateErrorStatus(
      TestScenario resource, Context<TestScenario> context, Exception e
  ) {
    resource.getStatus().setErrorMessage(e.getMessage());
    return ErrorStatusUpdateControl.patchStatus(resource);
  }
}
