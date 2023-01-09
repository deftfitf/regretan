package io.regretan.worker.scenario;

import io.regretan.customresource.dsl.EmptyAction;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.TestScenarioDto;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmptyActionRunner implements ScenarioRunner<EmptyAction> {

  @Override
  public String kind() {
    return "empty";
  }

  @Override
  public Class<EmptyAction> testActionClass() {
    return EmptyAction.class;
  }

  @Override
  public Map<String, String> run(
      TestScenarioDto scenario, ScenarioExecutionDto execution,
      int step, EmptyAction testAction
  ) {
    log.info("run EmptyAction: executionId={}, step={}", execution.getExecutionId(), step);
    return Map.of();
  }

}
