package io.regretan.scheduler.repository;

import io.regretan.mapper.RegretanInfoMapper;
import io.regretan.mapper.dto.RegretanInfoDto;
import io.regretan.setting.RegretanSetting;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RegretanInfoRepository {

  private final @NonNull RegretanSetting regretanSetting;
  private final @NonNull RegretanInfoMapper regretanInfoMapper;

  public Optional<RegretanInfoDto> find() {
    return Optional.ofNullable(regretanInfoMapper.find());
  }

  public void updateLastScheduledTime(LocalDateTime lastScheduled) {
    regretanInfoMapper.insert(regretanSetting.getHostname(), lastScheduled);
  }

}
