package io.regretan.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.regretan.worker.scenario.ScenarioRunner;
import io.regretan.worker.scenario.ScenarioRunnerRegistry;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleConfiguration {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ScenarioRunnerRegistry scenarioRunnerRegistry(
      List<ScenarioRunner<?>> scenarioRunners
  ) {
    return ScenarioRunnerRegistry.createRegistry(scenarioRunners);
  }

}
