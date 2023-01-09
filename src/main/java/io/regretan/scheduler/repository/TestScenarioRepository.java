package io.regretan.scheduler.repository;

import io.regretan.mapper.TestScenarioMapper;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.TestScenarioDto;
import io.regretan.util.TransactionUtils;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TestScenarioRepository {

  private final @NonNull TestScenarioMapper testScenarioMapper;
  private final @NonNull ConcurrentHashMap<CacheKey, TestScenarioDto> scenarioCache =
      new ConcurrentHashMap<>();

  public void create(TestScenarioDto testScenarioDto) {
    testScenarioMapper.insertScenario(testScenarioDto);

    TransactionUtils.afterCommit(() -> {
      scenarioCache.put(CacheKey.from(testScenarioDto), testScenarioDto);
    });
  }

  public void markAsDelete(TestScenarioDto testScenarioDto) {
    scenarioCache.put(CacheKey.from(testScenarioDto), testScenarioDto);

    testScenarioMapper.markAsDelete(
        testScenarioDto.getNamespace(),
        testScenarioDto.getScenarioName());
  }

  public List<TestScenarioDto> findActiveScenarios() {
    return scenarioCache.values()
        .stream()
        .filter(Predicate.not(TestScenarioDto::isDeleted))
        .toList();
  }

  public List<ScenarioExecutionDto> findRemainingExecutions(TestScenarioDto testScenarioDto) {
    return testScenarioMapper.selectRemainingExecutions(
        testScenarioDto.getNamespace(),
        testScenarioDto.getScenarioName());
  }

  public void reschedulePendingJobImmediately(TestScenarioDto testScenarioDto) {
    testScenarioMapper.rescheduleAllPendingJobsImmediately(
        testScenarioDto.getNamespace(),
        testScenarioDto.getScenarioName());
  }

  public Collection<ScenarioExecutionDto> findScenariosToResume(LocalDateTime scheduleTime) {
    return testScenarioMapper.selectScenarioToResume(scheduleTime);
  }

  public void delete(TestScenarioDto testScenarioDto) {
    TransactionUtils.beforeCommit(() -> {
      final var cacheKey = CacheKey.from(testScenarioDto);
      scenarioCache.remove(cacheKey);
      log.info("Removed scenario cache before transaction committed "
          + "so as not to execute deleted scenarios. key={}", cacheKey);
    });

    final var target = testScenarioMapper.selectScenarioForUpdate(
        testScenarioDto.getNamespace(),
        testScenarioDto.getScenarioName());
    if (target.isEmpty()) {
      log.info("The testScenario has already been deleted though the delete method was called."
              + "namespace={}, scenario_name={}, generation={}",
          testScenarioDto.getNamespace(),
          testScenarioDto.getScenarioName(),
          testScenarioDto.getGeneration());
      return;
    }

    testScenarioMapper
        .archiveScenarioExecution(
            testScenarioDto.getNamespace(),
            testScenarioDto.getScenarioName());
    testScenarioMapper
        .deleteScenarioExecution(
            testScenarioDto.getNamespace(),
            testScenarioDto.getScenarioName());
    testScenarioMapper
        .deleteScenario(
            testScenarioDto.getNamespace(),
            testScenarioDto.getScenarioName());
  }

  public record CacheKey(String namespace, String scenarioName) {

    public static CacheKey from(TestScenarioDto testScenarioDto) {
      return new CacheKey(
          testScenarioDto.getNamespace(),
          testScenarioDto.getScenarioName());
    }

  }

}
