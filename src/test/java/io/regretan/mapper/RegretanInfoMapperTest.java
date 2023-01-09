package io.regretan.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.regretan.mapper.libs.MapperTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RegretanInfoMapperTest extends MapperTest {

  @Test
  void insertAndFindRegretanInfo() {
    final var mapper = getMapper(RegretanInfoMapper.class);

    {
      final var hostname = "hostname1";
      final var lastScheduled = LocalDateTime.of(2023, 1, 1, 0, 0);
      mapper.insert(hostname, lastScheduled);
      assertThat(mapper.find()).satisfies(regretanInfoDto -> {
        assertThat(regretanInfoDto.getHostname()).isEqualTo(hostname);
        assertThat(regretanInfoDto.getLastScheduled()).isEqualTo(lastScheduled);
      });
    }

    {
      final var hostname = "hostname2";
      final var lastScheduled = LocalDateTime.of(2023, 1, 2, 0, 0);
      mapper.insert(hostname, lastScheduled);
      assertThat(mapper.find()).satisfies(regretanInfoDto -> {
        assertThat(regretanInfoDto.getHostname()).isEqualTo(hostname);
        assertThat(regretanInfoDto.getLastScheduled()).isEqualTo(lastScheduled);
      });
    }
  }

}