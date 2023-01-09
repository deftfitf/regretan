package io.regretan.mapper;

import io.regretan.mapper.dto.ScenarioExecutionArchiveDto;
import io.regretan.mapper.dto.ScenarioExecutionDto;
import io.regretan.mapper.dto.TestScenarioDto;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TestScenarioMapper {

  @Insert("""
      INSERT test_scenario (
        namespace, scenario_name, generation, scenario
      ) VALUES (
        #{namespace}, #{scenarioName}, #{generation}, #{scenario}
      )
      ON DUPLICATE KEY UPDATE scenario = values(scenario)
      """)
  int insertScenario(TestScenarioDto scenarioDto);

  @Insert("""
      INSERT INTO scenario_execution (
        namespace, scenario_name, generation, status, scheduled_time
      ) VALUES (
        #{namespace}, #{scenarioName}, #{generation}, #{status}, #{scheduledTime}
      )
      """)
  @Options(useGeneratedKeys = true, keyProperty = "executionId", keyColumn = "execution_id")
  int insertScenarioExecution(ScenarioExecutionDto executionDto);

  @Update("""
      UPDATE scenario_execution
      SET status = 'FAILED',
          failed_reason = #{failedReason}
      WHERE execution_id = #{executionId}
      """)
  int failedExecution(
      @Param("executionId") long executionId,
      @Param("failedReason") String failedReason);

  @Update("""
      UPDATE scenario_execution
      SET status = 'PENDING',
          next_step = #{nextStep},
          next_step_time = #{nextStepTime},
          failed_reason = NULL
      WHERE execution_id = #{executionId}
      """)
  int pendExecution(
      @Param("executionId") long executionId,
      @Param("nextStep") int nextStep,
      @Param("nextStepTime") LocalDateTime nextStepTime);

  @Update("""
      UPDATE scenario_execution
      SET next_step_time = CURRENT_TIMESTAMP()
      WHERE namespace = #{namespace}
        AND scenario_name = #{scenarioName}
        AND status = 'PENDING'
      """)
  int rescheduleAllPendingJobsImmediately(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName);

  @Update("""
      UPDATE scenario_execution
      SET status = 'FINISHED',
          next_step_time = NULL,
          end_time = CURRENT_TIMESTAMP()
      WHERE execution_id = #{executionId}
      """)
  int finishExecution(@Param("executionId") long executionId);

  @Update("""
      UPDATE scenario_execution
      SET status = 'RUNNING',
          next_step_time = NULL
      WHERE execution_id = #{executionId}
        AND status = 'PENDING'
      """)
  int resumeExecution(@Param("executionId") long executionId);

  @Update("""
      UPDATE scenario_execution
      SET next_step = #{nextStep},
          next_step_time = NULL,
          failed_reason = NULL
      WHERE execution_id = #{executionId}
      """)
  int updateStep(
      @Param("executionId") long executionId,
      @Param("nextStep") int nextStep);

  @Select("""
      <script>
      SELECT *
      FROM scenario_execution
      WHERE execution_id = #{executionId}
      <if test="forUpdate">
      FOR UPDATE
      </if>
      </script>
      """)
  ScenarioExecutionDto findExecutionById(
      @Param("executionId") long executionId,
      @Param("forUpdate") boolean forUpdate);

  @Select("""
      SELECT *
      FROM scenario_execution
      WHERE status = 'PENDING'
        AND next_step_time <= #{nextStepTime}
      """)
  List<ScenarioExecutionDto> selectScenarioToResume(
      @Param("nextStepTime") LocalDateTime nextStepTime
  );

  @Select("""
      SELECT *
      FROM scenario_execution
      WHERE namespace = #{namespace}
        AND scenario_name = #{scenarioName}
        AND (
          -- whether there are executions, which must be exclusive from new execution.
          status IN ('RUNNING', 'PENDING')
          OR (
            -- whether there are finished executions, which was scheduled at the same time.
            status = 'FINISHED'
            AND scheduled_time = #{scheduledTime}
          )
        )
      FOR UPDATE
      """)
  List<ScenarioExecutionDto> selectExclusiveExecutions(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName,
      @Param("scheduledTime") String scheduledTime);

  @Select("""
      SELECT *
      FROM scenario_execution
      WHERE namespace = #{namespace}
        AND scenario_name = #{scenarioName}
        AND status IN ('RUNNING', 'PENDING')
      FOR UPDATE
      """)
  List<ScenarioExecutionDto> selectRemainingExecutions(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName);

  @Delete("""
      UPDATE test_scenario
         SET is_deleted = TRUE
      WHERE namespace = #{namespace}
      AND scenario_name = #{scenarioName}
      """)
  int markAsDelete(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName);

  @Delete("""
      DELETE FROM test_scenario
      WHERE namespace = #{namespace}
      AND scenario_name = #{scenarioName}
      """)
  int deleteScenario(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName
  );

  @Delete("""
      DELETE FROM scenario_execution
      WHERE namespace = #{namespace}
      AND scenario_name = #{scenarioName}
      """)
  int deleteScenarioExecution(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName
  );

  @Delete("""
      INSERT INTO scenario_execution_archive (
        execution_id, namespace, scenario_name, generation,
        status, scheduled_time, start_time, end_time
      )
      SELECT
        execution_id, namespace, scenario_name, generation,
        status, scheduled_time, start_time, end_time
      FROM scenario_execution
      WHERE namespace = #{namespace}
      AND scenario_name = #{scenarioName} 
      """)
  int archiveScenarioExecution(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName);

  @Select("""
      SELECT *
      FROM scenario_execution_archive
      WHERE namespace = #{namespace}
        AND scenario_name = #{scenarioName} 
      """)
  List<ScenarioExecutionArchiveDto> selectExecutionArchive(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName);

  @Select("""
      <script>
      SELECT *
      FROM test_scenario
      WHERE namespace = #{namespace}
      AND scenario_name = #{scenarioName}
      AND generation = #{generation}
      LIMIT 1
      <if test="forUpdate">
      FOR UPDATE
      </if>
      </script>
      """)
  TestScenarioDto findScenario(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName,
      @Param("generation") long generation,
      @Param("forUpdate") boolean forUpdate
  );

  @Select("""
      SELECT *
      FROM test_scenario
      WHERE namespace = #{namespace}
      AND scenario_name = #{scenarioName}
      FOR UPDATE
      """)
  List<TestScenarioDto> selectScenarioForUpdate(
      @Param("namespace") String namespace,
      @Param("scenarioName") String scenarioName);

}
