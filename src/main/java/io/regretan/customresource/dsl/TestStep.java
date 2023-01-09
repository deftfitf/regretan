package io.regretan.customresource.dsl;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import java.util.List;
import lombok.Data;

/**
 * The TestStep abstract class represents individual steps in the test scenario. Each TestStep is
 * declared in CustomResourceDefinition, which is written by users who want to conduct E2E testing.
 */
@Data
@JsonDeserialize(using = JsonDeserializer.None.class)
public class TestStep implements KubernetesResource {

  /**
   * Actual action to do something for test.
   */
  @JsonPropertyDescription("Actual action to do something for test.")
  private TestAction action;

  /**
   * For cheking whether the step is done correctly. To pass this test step, all the matchers must
   * be true. If no matchers is specified, it will be assumed to be true.
   */
  @JsonPropertyDescription(
      """
          Matchers for cheking whether the step is done correctly.
          To pass this test step, all the matchers must be true.
          If no matchers is specified, it will be assumed to be true.""")
  private List<Matcher> matchers;

}
