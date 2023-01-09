package io.regretan.mapper;

import io.regretan.mapper.dto.RegretanInfoDto;
import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RegretanInfoMapper {

  @Select("SELECT hostname, last_scheduled FROM regretan_info WHERE id = 0")
  RegretanInfoDto find();

  @Insert("""
      INSERT INTO regretan_info (id, hostname, last_scheduled)
      VALUES (0, #{hostname}, #{lastScheduled})
      ON DUPLICATE KEY UPDATE
        hostname = values(hostname),
        last_scheduled = values(last_scheduled)
      """)
  int insert(
      @Param("hostname") String hostname,
      @Param("lastScheduled") LocalDateTime lastScheduled);

}
