package io.regretan.mapper.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.regretan.customresource.TestScenarioSpec.CronExpressionDeserializer;
import io.regretan.customresource.TestScenarioSpec.CronExpressionSerializer;
import io.regretan.customresource.dsl.TestAction;
import io.regretan.customresource.dsl.TestStep;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.scheduling.support.CronExpression;

@Data
public class TestScenarioDto {

  private String namespace;
  private String scenarioName;
  private long generation;
  private ScenarioDto scenario;
  private boolean isDeleted;

  public LocalDateTime getNextScheduleAfter(LocalDateTime baseTime) {
    return getScenario().getSchedule().next(baseTime);
  }

  public int getCleanupStep() {
    if (!isDefinedCleanup()) {
      throw new IllegalStateException("This method can be called when cleanup is not defined.");
    }
    return getStepNum() + 1;
  }

  public int getStepNum() {
    return getScenario().getTestSteps().size();
  }

  public TestStep getStepOf(int step) {
    return getScenario().getTestSteps().get(step - 1);
  }

  public boolean isDefinedCleanup() {
    return scenario.getCleanup() != null;
  }

  public TestAction getCleanupAction() {
    return scenario.getCleanup();
  }

  @Data
  public static class ScenarioDto {

    @JsonSerialize(using = CronExpressionSerializer.class)
    @JsonDeserialize(using = CronExpressionDeserializer.class)
    private CronExpression schedule;

    private List<TestStep> testSteps;

    private TestAction cleanup;
  }

}
