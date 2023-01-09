package io.regretan.worker.scenario;

import io.regretan.worker.exception.ScenarioRunnerUndefinedException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScenarioRunnerRegistry {

  private final Map<String, ScenarioRunner<?>> kindScenarioRunners;

  public static ScenarioRunnerRegistry createRegistry(List<ScenarioRunner<?>> scenarioRunners) {
    try {
      final var kindScenarioRunners = scenarioRunners
          .stream()
          .collect(Collectors.toMap(
              ScenarioRunner::kind,
              Function.identity()));

      return new ScenarioRunnerRegistry(kindScenarioRunners);
    } catch (IllegalStateException e) {
      throw new RuntimeException(
          "Exception occurs when registry ScenarioRunners. "
              + "Please check whether there are runners that has duplicated kind.", e);
    }
  }

  public ScenarioRunner<?> getScenarioRunner(String kind)
      throws ScenarioRunnerUndefinedException {
    final var scenarioRunner = kindScenarioRunners.get(kind);
    if (scenarioRunner == null) {
      throw new ScenarioRunnerUndefinedException("There is no ScenarioRunner: kind=" + kind);
    }
    return scenarioRunner;
  }

}
