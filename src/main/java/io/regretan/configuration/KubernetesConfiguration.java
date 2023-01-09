package io.regretan.configuration;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfiguration {

  @Bean
  public KubernetesClient kubernetesClient() {
    return new KubernetesClientBuilder()
        .withConfig(new ConfigBuilder().build())
        .build();
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  @SuppressWarnings("rawtypes")
  @ConditionalOnProperty(name = "regretan.role", havingValue = "scheduler")
  public Operator operator(KubernetesClient kubernetesClient, List<Reconciler> controllers) {
    Operator operator = new Operator(kubernetesClient);
    controllers.forEach(operator::register);
    return operator;
  }

}
