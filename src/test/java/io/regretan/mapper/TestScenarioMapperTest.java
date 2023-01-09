package io.regretan.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.regretan.customresource.dsl.EmptyAction;
import io.regretan.customresource.dsl.EqualMatcher;
import io.regretan.customresource.dsl.TestStep;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.ScenarioExecutionDto.ExecutionStatus;
import io.regretan.mapper.dto.TestScenarioDto;
import io.regretan.mapper.libs.MapperTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

class TestScenarioMapperTest extends MapperTest {

  @Test
  public void insertScenario() {
    final var mapper = getMapper(TestScenarioMapper.class);
    final var testScenario = createDummyScenario();

    mapper.insertScenario(testScenario);

    final var inserted = mapper.findScenario(
        testScenario.getNamespace(),
        testScenario.getScenarioName(),
        testScenario.getGeneration(),
        false);

    assertThat(inserted.getNamespace()).isEqualTo(testScenario.getNamespace());
    assertThat(inserted.getScenarioName()).isEqualTo(testScenario.getScenarioName());
    assertThat(inserted.getGeneration()).isEqualTo(testScenario.getGeneration());
    assertThat(inserted.getScenario()).isEqualTo(testScenario.getScenario());
  }

  @Test
  public void deleteScenario() {
    final var mapper = getMapper(TestScenarioMapper.class);
    final var testScenario = createDummyScenario();

    for (int i = 1; i <= 3; i++) {
      testScenario.setGeneration(i);
      mapper.insertScenario(testScenario);
      final var inserted = mapper.findScenario(
          testScenario.getNamespace(),
          testScenario.getScenarioName(),
          testScenario.getGeneration(),
          false);
      assertThat(inserted.getNamespace()).isEqualTo(testScenario.getNamespace());
      assertThat(inserted.getScenarioName()).isEqualTo(testScenario.getScenarioName());
      assertThat(inserted.getGeneration()).isEqualTo(testScenario.getGeneration());
      assertThat(inserted.getScenario()).isEqualTo(testScenario.getScenario());
    }

    final var allGenerations = mapper.selectScenarioForUpdate(
        testScenario.getNamespace(),
        testScenario.getScenarioName());
    assertThat(allGenerations.size()).isEqualTo(3);

    final var deleted = mapper.deleteScenario(
        testScenario.getNamespace(),
        testScenario.getScenarioName());
    assertThat(deleted).isEqualTo(3);

    assertThat(mapper.selectScenarioForUpdate(
        testScenario.getNamespace(),
        testScenario.getScenarioName())).asList().isEmpty();
  }

  @Test
  public void markAsDelete() {
    final var mapper = getMapper(TestScenarioMapper.class);
    final var testScenario = createDummyScenario();

    mapper.insertScenario(testScenario);
    final var inserted = mapper.findScenario(
        testScenario.getNamespace(),
        testScenario.getScenarioName(),
        testScenario.getGeneration(),
        false);
    assertThat(inserted.isDeleted()).isFalse();

    assertThat(mapper.markAsDelete(
        testScenario.getNamespace(),
        testScenario.getScenarioName())).isEqualTo(1);

    final var markedAsDeleted = mapper.findScenario(
        testScenario.getNamespace(),
        testScenario.getScenarioName(),
        testScenario.getGeneration(),
        false);

    assertThat(markedAsDeleted.isDeleted()).isTrue();
  }

  @Test
  public void insertScenarioExecution() {
    final var mapper = getMapper(TestScenarioMapper.class);

    final var testScenario = createDummyScenario();
    mapper.insertScenario(testScenario);

    final var execution = createScenarioExecutionDto();
    mapper.insertScenarioExecution(execution);
    // embedded execution id by mybatis
    assertThat(execution.getExecutionId()).isNotZero();

    final var inserted = mapper.findExecutionById(execution.getExecutionId(), false);
    assertThat(inserted.getExecutionId()).isEqualTo(execution.getExecutionId());
    assertThat(inserted.getNamespace()).isEqualTo(execution.getNamespace());
    assertThat(inserted.getScenarioName()).isEqualTo(execution.getScenarioName());
    assertThat(inserted.getGeneration()).isEqualTo(execution.getGeneration());
    assertThat(inserted.getNextStep()).isEqualTo(1);
    assertThat(inserted.getStatus()).isEqualTo(execution.getStatus());
    assertThat(inserted.getFailedReason()).isNull();
    assertThat(inserted.getScheduledTime()).isEqualTo(execution.getScheduledTime());
    assertThat(inserted.getStartTime()).isNotNull();
    assertThat(inserted.getNextStepTime()).isNull();
    assertThat(inserted.getEndTime()).isNull();

    {
      assertThat(mapper.updateStep(inserted.getExecutionId(), 2)).isEqualTo(1);
      final var updated = mapper.findExecutionById(execution.getExecutionId(), false);
      assertThat(updated.getNextStep()).isEqualTo(2);
      assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
    }

    {
      final var nextSchedule = LocalDateTime.of(2023, 1, 1, 0, 0);
      mapper.pendExecution(inserted.getExecutionId(), 3, nextSchedule);
      final var pended = mapper.findExecutionById(execution.getExecutionId(), false);
      assertThat(pended.getNextStep()).isEqualTo(3);
      assertThat(pended.getNextStepTime()).isEqualTo(nextSchedule);
      assertThat(pended.getStatus()).isEqualTo(ExecutionStatus.PENDING);
    }

    {
      assertThat(mapper.resumeExecution(inserted.getExecutionId())).isEqualTo(1);
      final var resumed = mapper.findExecutionById(execution.getExecutionId(), false);
      assertThat(resumed.getNextStep()).isEqualTo(3);
      assertThat(resumed.getNextStepTime()).isNull();
      assertThat(resumed.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
    }

    {
      assertThat(mapper.finishExecution(inserted.getExecutionId())).isEqualTo(1);
      final var finished = mapper.findExecutionById(execution.getExecutionId(), false);
      assertThat(finished.getNextStep()).isEqualTo(3);
      assertThat(finished.getNextStepTime()).isNull();
      assertThat(finished.getStatus()).isEqualTo(ExecutionStatus.FINISHED);
      assertThat(finished.getEndTime()).isNotNull();
    }

    final var execution2 = createScenarioExecutionDto();
    mapper.insertScenarioExecution(execution2);
    // embedded execution id by mybatis
    assertThat(execution2.getExecutionId()).isNotZero();
    assertThat(execution.getExecutionId()).isNotEqualTo(execution2.getExecutionId());
    assertThat(execution2.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
    mapper.failedExecution(execution2.getExecutionId(), "Exception occured");
    final var failed = mapper.findExecutionById(execution2.getExecutionId(), false);
    assertThat(failed.getStatus()).isEqualTo(ExecutionStatus.FAILED);
    assertThat(failed.getFailedReason()).isEqualTo("Exception occured");
  }

  @Test
  public void archiveScenarioExecution() {
    final var mapper = getMapper(TestScenarioMapper.class);
    final var testScenario = createDummyScenario();
    mapper.insertScenario(testScenario);

    for (int i = 0; i < 3; i++) {
      final var execution = createScenarioExecutionDto();
      mapper.insertScenarioExecution(execution);
      assertThat(execution.getExecutionId()).isNotZero();
    }

    assertThat(mapper.archiveScenarioExecution(
        testScenario.getNamespace(),
        testScenario.getScenarioName())).isEqualTo(3);
    assertThat(mapper.selectExecutionArchive(
        testScenario.getNamespace(),
        testScenario.getScenarioName())).asList().hasSize(3);

    assertThat(mapper.deleteScenarioExecution(
        testScenario.getNamespace(),
        testScenario.getScenarioName())).isEqualTo(3);
  }

  private TestScenarioDto createDummyScenario() {
    final var testScenario = new TestScenarioDto();
    testScenario.setNamespace("namespace");
    testScenario.setScenarioName("scenarioName");
    testScenario.setGeneration(1L);

    final var scenario = new TestScenarioDto.ScenarioDto();
    scenario.setSchedule(CronExpression.parse("0 0 12 * * ?"));

    final var testStep = new TestStep();
    testStep.setAction(new EmptyAction());
    testStep.setMatchers(List.of(new EqualMatcher()));
    scenario.setTestSteps(List.of(testStep));
    testScenario.setScenario(scenario);
    return testScenario;
  }

  private ScenarioExecutionDto createScenarioExecutionDto() {
    final var execution = new ScenarioExecutionDto();
    execution.setNamespace("namespace");
    execution.setScenarioName("scenarioName");
    execution.setGeneration(1L);
    execution.setStatus(ExecutionStatus.RUNNING);
    execution.setScheduledTime("scheduledTime");
    return execution;
  }

}