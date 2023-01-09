package io.regretan.scheduler;

import io.regretan.setting.RegretanSetting;
import io.regretan.worker.dto.QueueScenarioRequest;
import io.regretan.worker.dto.ResumeScenarioRequest;
import java.net.URI;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RegretanWorkerClient {

  private final @NonNull RegretanSetting regretanSetting;

  public void queueNewScenario(QueueScenarioRequest request) {
    final var restTemplate = new RestTemplate();
    final var queueAddress = String.format(
        "%s%s", regretanSetting.getWorker().getAddress(), "/v1/worker/queue");

    final var response = restTemplate.exchange(
        RequestEntity
            .post(URI.create(queueAddress))
            .body(request), Void.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException(String.format(
          "Attempt to queue new scenario was failed: uri=%s", queueAddress));
    }
  }

  public void resumeScenario(ResumeScenarioRequest request) {
    final var restTemplate = new RestTemplate();
    final var resumeAddress = String.format(
        "%s%s", regretanSetting.getWorker().getAddress(), "/v1/worker/resume");

    final var response = restTemplate.exchange(
        RequestEntity
            .post(URI.create(resumeAddress))
            .body(request), Void.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException(String.format(
          "Attempt to resume scenario was failed: uri=%s", resumeAddress));
    }
  }

}
