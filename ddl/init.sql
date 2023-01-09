CREATE TABLE IF NOT EXISTS `regretan_info`
(
    `id`             INT          NOT NULL,
    `hostname`       VARCHAR(255) NOT NULL,
    `last_scheduled` DATETIME     NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `test_scenario`
(
    `namespace`     VARCHAR(64) NOT NULL,
    `scenario_name` VARCHAR(64) NOT NULL,
    `generation`    INT         NOT NULL,
    `scenario`      MEDIUMTEXT  NOT NULL,
    `is_deleted`    BOOL        NOT NULL DEFAULT FALSE,
    `created`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`namespace`, `scenario_name`, `generation`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `scenario_execution`
(
    `execution_id`   BIGINT UNSIGNED                                  NOT NULL AUTO_INCREMENT,
    `namespace`      VARCHAR(64)                                      NOT NULL,
    `scenario_name`  VARCHAR(64)                                      NOT NULL,
    `generation`     INT                                              NOT NULL,
    `next_step`      SMALLINT UNSIGNED                                NOT NULL DEFAULT 1,
    `status`         ENUM ('RUNNING', 'PENDING','FINISHED', 'FAILED') NOT NULL,
    `failed_reason`  TEXT,
    `scheduled_time` VARCHAR(64)                                      NOT NULL,
    `start_time`     DATETIME                                         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `next_step_time` DATETIME                                                  DEFAULT NULL,
    `end_time`       DATETIME                                                  DEFAULT NULL,
    PRIMARY KEY (`execution_id`),
    FOREIGN KEY (`namespace`, `scenario_name`)
        REFERENCES `test_scenario` (`namespace`, `scenario_name`)
        ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `scenario_execution_archive`
(
    `archive_id`     BIGINT UNSIGNED                                  NOT NULL AUTO_INCREMENT,
    `execution_id`   BIGINT UNSIGNED                                  NOT NULL,
    `namespace`      VARCHAR(64)                                      NOT NULL,
    `scenario_name`  VARCHAR(64)                                      NOT NULL,
    `generation`     INT                                              NOT NULL,
    `status`         ENUM ('RUNNING', 'PENDING','FINISHED', 'FAILED') NOT NULL,
    `scheduled_time` VARCHAR(64)                                      NOT NULL,
    `start_time`     DATETIME                                         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_time`       DATETIME,
    PRIMARY KEY (`archive_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;