package io.regretan.setting;

import lombok.NonNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@ConstructorBinding
@ConfigurationProperties("regretan")
public class RegretanSetting {

  @NonNull String hostname;
  @NonNull String kubernetesPort;
  @NonNull RegretanWorkerSetting worker;

  @Value
  @Validated
  @ConstructorBinding
  public static class RegretanWorkerSetting {

    @NonNull String address;
    @NonNull Integer concurrency;
  }
}
