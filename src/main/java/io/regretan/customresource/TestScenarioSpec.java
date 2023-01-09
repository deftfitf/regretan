package io.regretan.customresource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.crd.generator.annotation.SchemaFrom;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.regretan.customresource.dsl.TestStep;
import java.io.IOException;
import java.util.List;
import lombok.Data;
import org.springframework.scheduling.support.CronExpression;

@Data
@JsonInclude(Include.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonPropertyOrder({"schedule", "steps", "disabled"})
public class TestScenarioSpec implements KubernetesResource {

  @JsonSerialize(using = CronExpressionSerializer.class)
  @JsonDeserialize(using = CronExpressionDeserializer.class)
  @SchemaFrom(type = String.class)
  @JsonPropertyDescription("Specify a schedule to run the scenario. see more detailed information: https://www.manpagez.com/man/5/crontab/")
  CronExpression schedule;

  @JsonPropertyDescription("Specify each step of the test scenario.")
  List<TestStep> steps;

  public static class CronExpressionSerializer extends JsonSerializer<CronExpression> {

    @Override
    public void serialize(CronExpression value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(value.toString());
    }

  }

  public static class CronExpressionDeserializer extends JsonDeserializer<CronExpression> {

    @Override
    public CronExpression deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      return CronExpression.parse(p.readValueAs(String.class));
    }

  }

}
