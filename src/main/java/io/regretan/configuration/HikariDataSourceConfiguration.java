package io.regretan.configuration;

import com.zaxxer.hikari.HikariDataSource;
import io.regretan.setting.HikariDataSourceSetting;
import java.util.List;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class HikariDataSourceConfiguration {

  @Bean
  public DataSource dataSource(HikariDataSourceSetting setting) {
    final var dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(setting.getJdbcUrl());
    dataSource.setUsername(setting.getUsername());
    dataSource.setPassword(setting.getPassword());
    dataSource.setMaximumPoolSize(setting.getMaximumPoolSize());
    dataSource.setMinimumIdle(setting.getMinimumIdle());
    dataSource.setIdleTimeout(setting.getIdleTimeout());
    dataSource.setMaxLifetime(setting.getMaxLifetime());
    dataSource.setReadOnly(setting.isReadOnly());
    dataSource.setConnectionInitSql(
        "SET SESSION sql_mode='TRADITIONAL,NO_AUTO_VALUE_ON_ZERO'");
    dataSource.setConnectionTimeout(setting.getConnectionTimeout().toMillis());
    dataSource.setConnectionTestQuery("SELECT 1");
    return dataSource;
  }

  @Bean
  public SqlSessionFactory sqlSessionFactory(
      DataSource dataSource, List<TypeHandler<?>> typeHandlerList
  ) throws Exception {
    final var resolver = ResourcePatternUtils
        .getResourcePatternResolver(new DefaultResourceLoader());
    final var factoryBean = new SqlSessionFactoryBean();
    factoryBean.setDataSource(dataSource);
    factoryBean.setConfigLocation(resolver.getResource("classpath:mybatis-config.xml"));
    typeHandlerList.forEach(factoryBean::setTypeHandlers);
    factoryBean.setFailFast(true);
    return factoryBean.getObject();
  }

  @Bean
  public DataSourceTransactionManager platformTransactionManager(DataSource dataSource) {
    final var manager = new DataSourceTransactionManager();
    manager.setDataSource(dataSource);
    return manager;
  }

}
