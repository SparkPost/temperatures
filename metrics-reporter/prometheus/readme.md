Steps to install prometheus, metrics reporting engine
--------------------------------------------------------
- https://prometheus.io/docs/prometheus/latest/installation/
- Running Prometheus on Docker is as simple as docker run -p 9090:9090 prom/prometheus. This starts Prometheus 
with a sample configuration and exposes it on port 9090.
- https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/deployment/metric_reporters/#prometheus , setup on flink
- http://localhost:9249/metrics - displays all the metrics logged
- For Parse record search filter:
  - # HELP flink_taskmanager_job_task_operator_kinesisanalytics_Temperature_record_parse_rate record-parse-rate (scope: taskmanager_job_task_operator_kinesisanalytics_Temperature)
    # TYPE flink_taskmanager_job_task_operator_kinesisanalytics_Temperature_record_parse_rate gauge
    - ```java
          processRate = getRuntimeContext().getMetricGroup()
                  .addGroup("kinesisanalytics")
                  .addGroup("Temperature", "ParseRecord")
                  .meter("record-parse-rate", new MeterView(60));
```
  - search for "record-parse-rate"

- http://localhost:9090/ , ui to filter metrics.