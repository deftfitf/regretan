package io.regretan.worker;

import io.regretan.worker.dto.QueueScenarioRequest;
import io.regretan.worker.dto.ResumeScenarioRequest;
import io.regretan.worker.exception.RegretanWorkerException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/worker")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "regretan.role", havingValue = "worker")
public class RegretanWorkerController {

  private final @NonNull RegretanWorker regretanWorker;

  @PostMapping("/queue")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void queueScenario(
      @RequestBody QueueScenarioRequest request
  ) {
    regretanWorker.runScenario(
        request.getNamespace(),
        request.getScenarioName(),
        request.getGeneration(),
        request.getScheduledTime());
  }

  @PostMapping("/resume")
  public void resumeScenario(
      @RequestBody ResumeScenarioRequest request
  ) {
    regretanWorker.resumeScenario(
        request.getExecutionId(),
        request.getNextStep());
  }

  @ExceptionHandler(RegretanWorkerException.class)
  public ResponseEntity<String> regretanWorkerExceptionHandler(
      Exception e, HttpServletRequest req, HttpServletResponse res
  ) {
    log.warn("RegretanWorkerException exception occurred: ", e);
    return new ResponseEntity<>(null, HttpStatus.OK);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> exceptionHandler(
      Exception e, HttpServletRequest req, HttpServletResponse res
  ) {
    log.error("Uncaught exception occurred: ", e);
    return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
