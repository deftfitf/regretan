package io.regretan.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.regretan.mapper.dto.TestScenarioDto.ScenarioDto;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScenarioSerde {

  private final ObjectMapper objectMapper;

  public String serialize(ScenarioDto scenario) {
    try {
      return objectMapper.writeValueAsString(scenario);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize scenario object", e);
    }
  }

  public ScenarioDto deserialize(String scenarioBytes) {
    try {
      return objectMapper.readValue(scenarioBytes, ScenarioDto.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to deserialize scenario object", e);
    }
  }

}
