package io.regretan.customresource.dsl;

import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotEqualMatcher extends Matcher {

  private Cond notEqual;

  @Override
  public MatchResult matches(Map<String, String> variables) {
    final var bindValue = bind(variables, notEqual.getValue());
    final var bindExpected = bind(variables, notEqual.getExpected());
    if (bindValue.equals(bindExpected)) {
      return new MatchResult(false,
          String.format("Failed to match! value: %s, but not expected to be: %s.",
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
