package io.regretan.customresource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.fabric8.kubernetes.client.utils.Serialization;
import io.regretan.customresource.dsl.EmptyAction;
import io.regretan.customresource.dsl.EqualMatcher;
import io.regretan.customresource.dsl.NotEqualMatcher;
import io.regretan.customresource.dsl.TestStep;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

class TestScenarioTest {

  @Test
  public void testScenarioCrdSpecSerde() {
    final var scenario = Serialization.unmarshal("""
        apiVersion: regretan.io/v1
        kind: TestScenario
        metadata:
          name: test-scenario-1
        spec:
          schedule: "0 * * * * ?"
          steps:
            - action:
                kind: empty
                name: empty-scenario-1
              matchers:
                - equal:
                    value: "{NULL}"
                    expected: "{NULL}"
            - action:
                kind: empty
                name: empty-scenario-2
                delay: 60s
              matchers:
                - notEqual:
                    value: "{NULL}"
                    expected: "NOT_NULL"
            - action:
                kind: empty
                name: empty-scenario-3
                delay: 120s
              matchers:
                - equal:
                    value: ""
                    expected: "{NULL}"
                - notEqual:
                    value: "{NULL}"
                    expected: "NOT_NULL"
        """, TestScenario.class);

    assertThat(scenario.getSpec()).isEqualTo(
        new TestScenarioSpec() {{
          setSchedule(CronExpression.parse("0 * * * * ?"));
          setSteps(List.of(
              new TestStep() {{
                setAction(new EmptyAction() {{
                  setName("empty-scenario-1");
                }});
                setMatchers(List.of(
                    new EqualMatcher() {{
                      setEqual(new Cond() {{
                        setValue("{NULL}");
                        setExpected("{NULL}");
                      }});
                    }}
                ));
              }},
              new TestStep() {{
                setAction(new EmptyAction() {{
                  setName("empty-scenario-2");
                  setDelay(Duration.ofSeconds(60));
                }});
                setMatchers(List.of(
                    new NotEqualMatcher() {{
                      setNotEqual(new Cond() {{
                        setValue("{NULL}");
                        setExpected("NOT_NULL");
                      }});
                    }}
                ));
              }},
              new TestStep() {{
                setAction(new EmptyAction() {{
                  setName("empty-scenario-3");
                  setDelay(Duration.ofSeconds(120));
                }});
                setMatchers(List.of(
                    new EqualMatcher() {{
                      setEqual(new Cond() {{
                        setValue("");
                        setExpected("{NULL}");
                      }});
                    }},
                    new NotEqualMatcher() {{
                      setNotEqual(new Cond() {{
                        setValue("{NULL}");
                        setExpected("NOT_NULL");
                      }});
                    }}
                ));
              }}
          ));
        }}
    );
  }

}