package io.regretan.customresource.dsl;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EqualMatcher extends Matcher {

  private Cond equal;

  @Override
  public MatchResult matches(Map<String, String> variables) {
    final var bindValue = bind(variables, equal.getValue());
    final var bindExpected = bind(variables, equal.getExpected());
    if (!bindValue.equals(bindExpected)) {
      return new MatchResult(false,
          String.format("Failed to match! value: %s, but expected: %s.",
              bindValue, bindExpected));
    }
    return new MatchResult(true, null);
  }

  @Data
  public static class Cond {

    String value;
    String expected;
  }

}
