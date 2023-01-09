package io.regretan.mapper.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RegretanInfoDto {

  private String hostname;
  private LocalDateTime lastScheduled;
}
