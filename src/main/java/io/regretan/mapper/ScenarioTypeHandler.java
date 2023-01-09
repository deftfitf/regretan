package io.regretan.mapper;

import io.regretan.mapper.dto.TestScenarioDto.ScenarioDto;
import io.regretan.serde.ScenarioSerde;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MappedTypes(ScenarioDto.class)
public class ScenarioTypeHandler extends BaseTypeHandler<ScenarioDto> {

  private final ScenarioSerde scenarioSerde;

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, ScenarioDto parameter, JdbcType jdbcType
  ) throws SQLException {
    ps.setString(i, scenarioSerde.serialize(parameter));
  }

  @Override
  public ScenarioDto getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return Optional
        .ofNullable(rs.getString(columnName))
        .map(scenarioSerde::deserialize)
        .orElse(null);
  }

  @Override
  public ScenarioDto getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return Optional
        .ofNullable(rs.getString(columnIndex))
        .map(scenarioSerde::deserialize)
        .orElse(null);
  }

  @Override
  public ScenarioDto getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return Optional
        .ofNullable(cs.getString(columnIndex))
        .map(scenarioSerde::deserialize)
        .orElse(null);
  }
}
