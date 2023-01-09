package io.regretan.customresource.dsl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class MatcherTest {

  public static class MockMatcher extends Matcher {

    @Override
    public MatchResult matches(Map<String, String> variables) {
      return null;
    }

  }

  @Test
  public void bind() {
    final var matcher = new MockMatcher();

    assertThat(matcher.bind(
        Map.of("FOO", "1", "BAR", "2", "BAZ", "3"),
        "{FOO}")).isEqualTo("1");
    assertThat(matcher.bind(
        Map.of("FOO", "1", "BAR", "2", "BAZ", "3"),
        "{FOO}_{BAR}")).isEqualTo("1_2");
    assertThat(matcher.bind(
        Map.of("FOO", "1", "BAR", "2", "BAZ", "3"),
        "{FOO}_{BAR}_{BAZ}")).isEqualTo("1_2_3");
    assertThat(matcher.bind(
        Map.of("FOO", "1", "BAR", "2", "BAZ", "3"),
        "{FOO}_{BAR}_{FOO}")).isEqualTo("1_2_1");
    assertThat(matcher.bind(
        Map.of("FOO", "1", "BAR", "2", "BAZ", "3"),
        "{FOO_BAR}_{FOO}")).isEqualTo("_1");
    assertThat(matcher.bind(
        Map.of("FOO", "1", "BAR", "2", "BAZ", "3"),
        "{FOO}_{BAR}_{BAR}_{FOO}")).isEqualTo("1_2_2_1");
  }

  @Test
  public void parse() throws JsonProcessingException {
    final var mapper = new YAMLMapper();

    {
      final var matcher = mapper.readValue(
          """
              equal:
                value: "{BAR}"
                expected: "{BAZ}"
              """,
          Matcher.class);
      assertThat(matcher)
          .isInstanceOf(EqualMatcher.class)
          .asInstanceOf(InstanceOfAssertFactories.type(EqualMatcher.class))
          .satisfies(equal -> {
            assertThat(equal.getEqual().getValue()).isEqualTo("{BAR}");
            assertThat(equal.getEqual().getExpected()).isEqualTo("{BAZ}");
          });
    }

    {
      final var matcher = mapper.readValue(
          """
              equal:
                expected: "{BAZ}"
                value: "{BAR}"
              """,
          Matcher.class);
      assertThat(matcher)
          .isInstanceOf(EqualMatcher.class)
          .asInstanceOf(InstanceOfAssertFactories.type(EqualMatcher.class))
          .satisfies(equal -> {
            assertThat(equal.getEqual().getValue()).isEqualTo("{BAR}");
            assertThat(equal.getEqual().getExpected()).isEqualTo("{BAZ}");
          });
    }

    {
      final var matcher = mapper.readValue(
          """
              notEqual:
                expected: "{BAZ}"
                value: "{BAR}"
              """,
          Matcher.class);
      assertThat(matcher)
          .isInstanceOf(NotEqualMatcher.class)
          .asInstanceOf(InstanceOfAssertFactories.type(NotEqualMatcher.class))
          .satisfies(equal -> {
            assertThat(equal.getNotEqual().getValue()).isEqualTo("{BAR}");
            assertThat(equal.getNotEqual().getExpected()).isEqualTo("{BAZ}");
          });
    }

    {
      final var matcher = mapper.readValue(
          """
              notEqual:
                value: "{BAR}"
                expected: "{BAZ}"
              """,
          Matcher.class);
      assertThat(matcher)
          .isInstanceOf(NotEqualMatcher.class)
          .asInstanceOf(InstanceOfAssertFactories.type(NotEqualMatcher.class))
          .satisfies(equal -> {
            assertThat(equal.getNotEqual().getValue()).isEqualTo("{BAR}");
            assertThat(equal.getNotEqual().getExpected()).isEqualTo("{BAZ}");
          });
    }

  }

}