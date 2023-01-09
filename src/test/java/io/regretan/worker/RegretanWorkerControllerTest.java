package io.regretan.worker;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import io.regretan.worker.dto.QueueScenarioRequest;
import io.regretan.worker.dto.ResumeScenarioRequest;
import io.regretan.worker.exception.RegretanWorkerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RegretanWorkerControllerTest {

  @MockBean
  private RegretanWorker regretanWorker;
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void queueScenario() {
    final var response = restTemplate.postForEntity(
        "/v1/worker/queue", QueueScenarioRequest
            .builder()
            .namespace("namespace")
            .scenarioName("scenario")
            .generation(1L)
            .scheduledTime("scheduledTime")
            .build(),
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    verify(regretanWorker)
        .runScenario("namespace", "scenario", 1L, "scheduledTime");
  }

  @Test
  public void returnOkWhenQueueScenarioIfRegretanWorkerExceptionThrown() {
    doThrow(new RegretanWorkerException("Error !"))
        .when(regretanWorker)
        .runScenario("namespace", "scenario", 1L, "scheduledTime");

    final var response = restTemplate.postForEntity(
        "/v1/worker/queue", QueueScenarioRequest
            .builder()
            .namespace("namespace")
            .scenarioName("scenario")
            .generation(1L)
            .scheduledTime("scheduledTime")
            .build(),
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void returnInternalErrorIfUncaughtExceptionOccurred() {
    doThrow(new IllegalStateException("Error !"))
        .when(regretanWorker)
        .runScenario("namespace", "scenario", 1L, "scheduledTime");

    final var response = restTemplate.postForEntity(
        "/v1/worker/queue", QueueScenarioRequest
            .builder()
            .namespace("namespace")
            .scenarioName("scenario")
            .generation(1L)
            .scheduledTime("scheduledTime")
            .build(),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isEqualTo("Internal Server Error");
  }

  @Test
  public void resumeScenario() {
    final var response = restTemplate.postForEntity(
        "/v1/worker/resume", ResumeScenarioRequest
            .builder()
            .executionId(1L)
            .nextStep(2)
            .build(),
        Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(regretanWorker)
        .resumeScenario(1L, 2);
  }

}