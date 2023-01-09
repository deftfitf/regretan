package io.regretan.customresource.dsl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotEqualMatcherTest {

  @CsvSource(value = {
      "{CASE_A}, success, false, 'Failed to match! value: success, but not expected to be: success.'",
      "{CASE_A}, failed, true, null",
      "{CASE_A}, {CASE_B}, false, 'Failed to match! value: success, but not expected to be: success.'",
      "{CASE_C}, {CASE_B}, true, null",
      "{CASE_A}_{CASE_C}, success_failed, false, 'Failed to match! value: success_failed, but not expected to be: success_failed.'",
      "success_failed, {CASE_A}_{CASE_C}, false, 'Failed to match! value: success_failed, but not expected to be: success_failed.'",
      "{CASE_A}_{CASE_B}, success_failed, true, null",
      "success_failed, {CASE_A}_{CASE_B}, true, null",
  }, nullValues = "null")
  @ParameterizedTest
  public void matches(
      String inputValue, String inputExpected,
      boolean expectedResult, String failedReason
  ) {
    final var matcher = new NotEqualMatcher() {{
      setNotEqual(new Cond() {{
        setValue(inputValue);
        setExpected(inputExpected);
      }});
    }};

    final var result = matcher.matches(Map.of(
        "CASE_A", "success",
        "CASE_B", "success",
        "CASE_C", "failed"
    ));
    assertThat(result.success()).isEqualTo(expectedResult);
    if (!result.success()) {
      assertThat(result.failedReason()).isEqualTo(failedReason);
    }
  }

}