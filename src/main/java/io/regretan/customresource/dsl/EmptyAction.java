package io.regretan.customresource.dsl;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The EmptyStep is a TestStep just for doing nothing. This will be used for debugging regretan
 * itself.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmptyAction extends TestAction {

  String kind = "empty";

}
