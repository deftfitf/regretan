package io.regretan.customresource.dsl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EqualMatcherTest {

  @CsvSource(value = {
      "{CASE_A}, success, true, null",
      "{CASE_A}, failed, false, 'Failed to match! value: success, but expected: failed.'",
      "{CASE_A}, {CASE_B}, true, null",
      "{CASE_C}, {CASE_B}, false, 'Failed to match! value: failed, but expected: success.'",
      "{CASE_A}_{CASE_C}, success_failed, true, null",
      "success_failed, {CASE_A}_{CASE_C}, true, null",
      "{CASE_A}_{CASE_B}, success_failed, false, 'Failed to match! value: success_success, but expected: success_failed.'",
      "success_failed, {CASE_A}_{CASE_B}, false, 'Failed to match! value: success_failed, but expected: success_success.'",
  }, nullValues = "null")
  @ParameterizedTest
  public void matches(
      String inputValue, String inputExpected,
      boolean expectedResult, String failedReason
  ) {
    final var matcher = new EqualMatcher() {{
      setEqual(new Cond() {{
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