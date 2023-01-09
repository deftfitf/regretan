package io.regretan.customresource;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group(TestScenario.GROUP)
@Version(TestScenario.VERSION)
@ShortNames({"ts"})
public class TestScenario
    extends CustomResource<TestScenarioSpec, TestScenarioStatus>
    implements Namespaced {

  public static final String GROUP = "regretan.io";
  public static final String VERSION = "v1";

}
