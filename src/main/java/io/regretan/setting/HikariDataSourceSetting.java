package io.regretan.setting;

import java.time.Duration;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@ConstructorBinding
@ConfigurationProperties("hikaricp-datasource")
public class HikariDataSourceSetting {

  String jdbcUrl;
  String username;
  String password;
  int maximumPoolSize;
  int minimumIdle;
  long idleTimeout;
  long maxLifetime;
  boolean readOnly;
  Duration connectionTimeout;
}
