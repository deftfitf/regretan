[![CI Pipeline](https://github.com/deftfitf/regretan/actions/workflows/ci.yaml/badge.svg?branch=main)](https://github.com/deftfitf/regretan/actions/workflows/ci.yaml)
[![DockerHub](https://img.shields.io/docker/v/deftfitf/regretan)](https://hub.docker.com/r/deftfitf/regretan)

# Regretan

[WARN] Only the minimum functions are implemented, and functions that can actually be used for
testing are not available.

---

Regretan is an operator application integrated with Kubernetes for E2E testing. By defining a
TestScenario custom resource using a pre-prepared DSL and registering it in Kubernetes, it will be
automatically added as a management target.

Execution of TestScenario is scheduled by CronExpression. You can also set a delay for each step and
reschedule, making it easier to manage long-running test cases.

A test case is defined as a CustomResource as below.

```yaml
apiVersion: regretan.io/v1
kind: TestScenario
metadata:
  name: test-scenario-1
spec:
  schedule: "0 * * * * ?"
  steps:
    - action:
        kind: empty
        name: empty-scenario-1
      matchers:
        - equal:
            value: "{NULL}"
            expected: "{NULL}"
    - action:
        kind: empty
        name: empty-scenario-2
        delay: 60s
      matchers:
        - notEqual:
            value: "{NULL}"
            expected: "NOT_NULL"
    - action:
        kind: empty
        name: empty-scenario-3
        delay: 120s
      matchers:
        - equal:
            value: ""
            expected: "{NULL}"
        - notEqual:
            value: "{NULL}"
            expected: "NOT_NULL"
```

## Features

### Actions

Action defines an action that needs to do something for the test.

#### EmptyAction

EmptyAction is an action that does nothing. It can be used when you simply want to delay the
evaluation of the Matcher.

```yaml
action:
  kind: empty
  name: empty-scenario
  delay: 60s
```

### Matchers

Matcher defines a matcher to evaluate the desired state as a result of previous actions. Each
matcher can embed a placeholder that is a combination of alphanumeric characters and _ in braces
like `{FOO}`. Variables resulting from Actions can be expanded into matchers.

#### EqualMatcher

EqualMatcher is a matcher that expects value and expected to match.

```yaml
equal:
  value: "{RESULT_VALUE}"
  expected: "true"
```

#### NotEqualMatcher

EqualMatcher is a matcher that expects value and expected not to match.

```yaml
notEqual:
  value: "{RESULT_VALUE}"
  expected: "false"
```

## Getting Started

### Install MySQL

Regretan uses MySQL to manage scheduling information and TestScenario data. You can install MySQL by
any method, but I will describe how to install MySQL using helm.

* See: [MySQL Helm Chart](https://github.com/bitnami/charts/tree/main/bitnami/mysql)

Create values.yaml to satisfy Regretan requirements as following.

```yaml
namespaceOverride: regretan

image:
  tag: '8.0'

architecture: standalone

auth:
  rootPassword: root_password
  createDatabase: true
  database: regretan
  username: regretan
  password: password

primary:
  extraEnvVars:
    - name: TZ
      value: "Asia/Tokyo"
```

Add the helm repository and install MySQL using the created values.yaml.

```
$ helm repo add my-repo https://charts.bitnami.com/bitnami
$ helm install my-release my-repo/mysql -f init/values.yaml
```

After successful installation, you should run `ddl/init.sql` directly to create the required tables.

### Apply k8s manifests

Apply each manifest for the installation of Regretan.

```shell
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/service-account.yaml
kubectl apply -f k8s/testscenarioes.regretan.io-v1.yaml
kubectl apply -f k8s/worker-deployment.yaml
kubectl apply -f k8s/scheduler-deployment.yaml
```

### Apply TestScenario custom resource

Create a test scenario and register it. Let's check using `sample/test-scenario.yaml`.

```shell
kubectl apply -f sample/test-scenario.yaml
```

Once registered, it will automatically be registered with MySQL as well.

```
mysql> select * from test_scenario LIMIT 1;
+-----------+-----------------+------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------+---------------------+
| namespace | scenario_name   | generation | scenario                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | is_deleted | created             |
+-----------+-----------------+------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------+---------------------+
| default   | test-scenario-1 |          2 | {"schedule":"0 * * * * ?","testSteps":[{"action":{"kind":"empty","kind":"empty","name":"empty-scenario-1","delay":null},"matchers":[{"equal":{"value":"${NULL}","expected":"${NULL}"}}]},{"action":{"kind":"empty","kind":"empty","name":"empty-scenario-2","delay":"60000ms"},"matchers":[{"notEqual":{"value":"${NULL}","expected":"NOT_NULL"}}]},{"action":{"kind":"empty","kind":"empty","name":"empty-scenario-3","delay":"120000ms"},"matchers":[{"equal":{"value":"","expected":"${NULL}"}},{"notEqual":{"value":"${NULL}","expected":"NOT_NULL"}}]}],"cleanup":null} |          0 | 2023-01-09 16:17:44 |
+-----------+-----------------+------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+------------+---------------------+
1 row in set (0.00 sec)
```

Scheduling starts immediately and you can see it running.

```
mysql> select * from scenario_execution ORDER BY end_time DESC LIMIT 5;
+--------------+-----------+-----------------+------------+-----------+----------+---------------+---------------------+---------------------+----------------+---------------------+
| execution_id | namespace | scenario_name   | generation | next_step | status   | failed_reason | scheduled_time      | start_time          | next_step_time | end_time            |
+--------------+-----------+-----------------+------------+-----------+----------+---------------+---------------------+---------------------+----------------+---------------------+
|           19 | default   | test-scenario-1 |          3 |         4 | FINISHED | NULL          | 2023-01-09T17:10:00 | 2023-01-09 17:10:00 | NULL           | 2023-01-09 17:11:00 |
|           18 | default   | test-scenario-1 |          3 |         4 | FINISHED | NULL          | 2023-01-09T17:08:00 | 2023-01-09 17:08:00 | NULL           | 2023-01-09 17:09:00 |
|           17 | default   | test-scenario-1 |          3 |         4 | FINISHED | NULL          | 2023-01-09T17:06:00 | 2023-01-09 17:06:00 | NULL           | 2023-01-09 17:07:01 |
|           16 | default   | test-scenario-1 |          3 |         4 | FINISHED | NULL          | 2023-01-09T17:04:00 | 2023-01-09 17:04:00 | NULL           | 2023-01-09 17:05:01 |
|           15 | default   | test-scenario-1 |          3 |         4 | FINISHED | NULL          | 2023-01-09T17:02:00 | 2023-01-09 17:02:00 | NULL           | 2023-01-09 17:03:01 |
+--------------+-----------+-----------------+------------+-----------+----------+---------------+---------------------+---------------------+----------------+---------------------+
5 rows in set (0.00 sec)
```

## Future Work

There are no useful features at the moment, but we may implement the following features in the
future.

* Implementing a UI that allows you to check the test status
* A scheduling process that repairs a Scenario Execution that remains RUNNING when a Worker goes
  down while processing.
* Implementation of Notifier and its setting screen that can send notifications to Slack etc. when
  processing succeeds / fails
* Expansion of TestAction/Matcher that can actually be used for E2E. HttpAction, etc.
* To implement your own CustomActions and CustomMatchers for more flexibility, you need to be able
  to implement your own RegretanWorker. Framework of RegretanWorker to realize this.