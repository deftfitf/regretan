package io.regretan.customresource.dsl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Data;

/**
 * The Matcher interface is for checking whether the test step was passed or not.
 */
@Data
@JsonTypeInfo(use = Id.DEDUCTION)
@JsonSubTypes({
    @Type(value = EqualMatcher.class),
    @Type(value = NotEqualMatcher.class),
})
public abstract class Matcher {

  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z](_?[a-zA-Z])+)\\}");

  abstract public MatchResult matches(Map<String, String> variables);

  public record MatchResult(boolean success, String failedReason) {

  }

  protected String bind(Map<String, String> variables, String value) {
    final var matcher = VARIABLE_PATTERN.matcher(value);
    final var binded = new StringBuilder();
    while (matcher.find()) {
      final var variableName = matcher.group(1);
      final var variable = Optional
          .ofNullable(variables.get(variableName))
          .orElse("");
      matcher.appendReplacement(binded, variable);
    }
    matcher.appendTail(binded);
    return binded.toString();
  }

}