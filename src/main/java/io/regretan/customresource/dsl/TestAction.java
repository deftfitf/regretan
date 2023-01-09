package io.regretan.customresource.dsl;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
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
import java.io.IOException;
import java.time.Duration;
import lombok.Data;
import org.springframework.boot.convert.DurationStyle;

@Data
@JsonTypeInfo(use = Id.NAME, property = "kind", defaultImpl = EmptyAction.class)
@JsonSubTypes({
    @Type(value = EmptyAction.class, name = "empty"),
})
@JsonDeserialize(using = JsonDeserializer.None.class)
public abstract class TestAction implements KubernetesResource {

  /**
   * The kind of TestAction. If not specified, EmptyAction is used by default.
   */
  @JsonPropertyDescription("Specify the kind of TestAction. If not specified, EmptyAction is used by default.")
  private String kind;

  /**
   * The name of TestAction. This name will be used in places such as notifications and reports.
   */
  @JsonPropertyDescription("Specify the name of TestAction. This name will be used in places such as notifications and reports.")
  private String name;

  /**
   * Specifies the delay to wait before starting the TestAction. Specifies if a long wait is
   * required after performing the previous Action.
   */
  @JsonSerialize(using = DurationSerializer.class)
  @JsonDeserialize(using = DurationDeserializer.class)
  @SchemaFrom(type = String.class)
  @JsonPropertyDescription("The delay to wait before starting the TestAction. Specifies if a long wait is required after performing the previous Action.")
  private Duration delay;

  public static class DurationSerializer extends JsonSerializer<Duration> {

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(DurationStyle.SIMPLE.print(value));
    }

  }

  public static class DurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      return DurationStyle.SIMPLE.parse(p.readValueAs(String.class));
    }

  }
}
