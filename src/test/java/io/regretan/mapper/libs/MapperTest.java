package io.regretan.mapper.libs;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class MapperTest {

  private static final String MAPPER_PACKAGE_NAME = "io.regretan";
  private static SqlSessionFactory sqlSessionFactory;
  protected SqlSession sqlSession;

  @BeforeAll
  public static void initialize() {
    sqlSessionFactory = new TestSqlSessionFactoryProducer()
        .getSqlSessionFactory(MAPPER_PACKAGE_NAME);
  }

  @BeforeEach
  public void setup() {
    sqlSession = sqlSessionFactory.openSession(false);
  }

  @AfterEach
  public void teardown() {
    sqlSession.rollback();
    sqlSession.close();
  }

  protected <T> T getMapper(final Class<T> mapperClass) {
    return sqlSession.getMapper(mapperClass);
  }
}