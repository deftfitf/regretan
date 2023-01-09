package io.regretan.mapper.libs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.regretan.mapper.ScenarioTypeHandler;
import io.regretan.serde.ScenarioSerde;
import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class TestSqlSessionFactoryProducer {

  private volatile SqlSessionFactory sqlSessionFactory;

  public SqlSessionFactory getSqlSessionFactory(String mapperPackageName) {
    if (sqlSessionFactory == null) {
      synchronized (this) {
        if (sqlSessionFactory == null) {
          sqlSessionFactory = buildSqlSessionFactory(mapperPackageName);
        }
      }
    }
    return sqlSessionFactory;
  }

  private static SqlSessionFactory buildSqlSessionFactory(String mapperPackageName) {
    try (InputStream mybatisConfigStream =
        Resources.getResourceAsStream("mybatis-test.xml")
    ) {
      final SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder()
          .build(mybatisConfigStream, "test");
      sqlSessionFactory.getConfiguration()
          .getTypeHandlerRegistry()
          .register(new ScenarioTypeHandler(new ScenarioSerde(new ObjectMapper())));
      sqlSessionFactory.getConfiguration()
          .addMappers(mapperPackageName);
      return sqlSessionFactory;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
