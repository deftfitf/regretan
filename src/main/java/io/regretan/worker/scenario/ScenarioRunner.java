package io.regretan.worker.scenario;

import io.regretan.customresource.dsl.TestAction;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.TestScenarioDto;
import java.util.Map;

public interface ScenarioRunner<T extends TestAction> {

  String kind();

  Class<T> testActionClass();

  Map<String, String> run(
      TestScenarioDto scenario,
      ScenarioExecutionDto execution,
      int step, T testAction);

}
