# Sending other Observability signals: Logs 


## Goal of this activity


In the previous sections, we discussed the fundamentals of tracing and the process of sending metrics to understand and monitor the performance of our applications. our next focus shifts to an equally important component: log management through OpenTelemetry.

Logs provide the detailed narrative of an application's operation, offering detailed insights into its behavior and interactions. While metrics and traces give us an overview and a deep dive into the performance issues, respectively, logs bring the granularity of textual data that can pinpoint exactly what happened and when. This level of detail is crucial for troubleshooting, security audits, and understanding the operational context of applications and systems.

OpenTelemetry extends its capabilities beyond tracing and metrics to address log management. This integration allows developers and operators to correlate logs with traces and metrics, creating a holistic view of their system's health and performance. By treating logs as a first-class citizen in the observability realm, OpenTelemetry facilitates a more nuanced and interconnected approach to monitoring.

This section of our lab will guide you through the essentials of log management in OpenTelemetry. 


## Main steps

In section 9, we explored how to send metrics. This section will not involve many changes regarding our code. We will reuse the same service from which we were collecting traces and metrics already. We will focus on the necessary steps to deal with log management. 

Here are some of the steps we will follow:

- Otel approach to send logs (Fluentbit, Filelog, Otel appender)
- Collector adjustements to enable filelog receiver 
- Collector adjustements to configure unified service tagging and ddsource (processor and pipelines)
- Adapting our Springboot service to generate log events to a file (Logback and MDC - implementation("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:2.1.0-alpha")
- Configuring logback
- Backend: DD log pipelines and remapper


## Otel approaches to send logs 

Otel provides thtree strategies for sending logs from applications to the collector, each catering to different logging needs and architectures. Here's a brief description of them:

### 1. **Direct send from application to the collector using Fluent Bit**

In this strategy, logs are sent directly from the application to the OpenTelemetry Collector using Fluent Bit. Fluent Bit is a lightweight and highly efficient log processor and forwarder, which can be embedded in the application or run as a separate service. This approach is designed for real-time log processing and forwarding, minimizing latency and overhead by bypassing intermediary log files. It's particularly suitable for cloud-native applications that demand fast, efficient, and direct log shipping without the need for persistent storage or additional log processing steps. The direct integration with Fluent Bit allows for flexible and powerful log processing capabilities, such as filtering, enrichment, and transformation, before the logs are sent to the OpenTelemetry Collector.

<p align="left">
  <img src="img/springotel101.png" width="850" />
</p>

### 2. **Based on FileLog: Watching logs and parsing them before sending to the backend**

The second strategy involves watching and parsing log files before they are sent to the backend. This method is typically implemented using the FileLog receiver in the OpenTelemetry Collector, which monitors log files for changes, reads new log entries, and processes them according to configured parsing rules. This strategy is well-suited for applications that write logs to files, providing a way to integrate with existing logging mechanisms without requiring changes to the application's logging configuration. It allows for the collection of logs that are written in various formats, transforming them into a structured format before forwarding them to the backend. This method is beneficial for batch processing, historical log analysis, and scenarios where logs need to be retained in files for compliance or auditing purposes.

<p align="left">
  <img src="img/springotel100.png" width="850" />
</p>

### 3. **Based on an OpenTelemetry log appender 

These initiatives represent a leap towards modernization. OpenTelemetry outlines best practices and guidance for emitting logs, traces, and metrics from these newly developed applications. For those languages and frameworks supported, employing auto-instrumentation or merely configuring a logging library to utilize an OpenTelemetry log appender often remains the most straightforward method for producing logs enriched with context. As previously discussed, ertain widely-used logging libraries across various programming languages have been enhanced to facilitate manual instrumentation efforts. These enhancements enable the integration of trace context within logs and permit the direct transmission of log data to the backend or to the Collector via the OTLP protocol, eliminating the need for logs to be stored as text files. Logs emitted in this manner are automatically enriched with specific resource contexts relevant to the application (for example, process ID, programming language, name and version of the logging library, etc.), ensuring comprehensive correlation across all dimensions of context for these logs.


This is how a typical new application uses OpenTelemetry API, SDK and the existing log libraries:

<p align="left">
  <img src="img/springotel103.png" width="850" />
</p>


These strategies offer distinct advantages and can be chosen based on the specific requirements of your logging architecture, such as the need for real-time processing, integration with existing logging systems, log data format, and compliance requirements.


## The Filelog receiver

In the rest of lab we will use filelog to showcase how sending logs can be done. 
Enabling the filelog receiver is fairly easy and requires adapting the collector configuration file to enable it.

Here is an example of configuration file with filelog enabled:

```yaml
receivers:
  otlp:
    protocols:
      http:
      grpc:
  filelog:
    include: [ /var/log/test/simple.log ]
    operators:
      - type: regex_parser
        regex: '^(?P<time>\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) (?P<sev>[A-Z]*) (?P<msg>.*)$'
        timestamp:
          parse_from: attributes.time
          layout: '%Y-%m-%d %H:%M:%S'
        severity:
          parse_from: attributes.sev      
processors:
  batch:
    timeout: 10s
connectors:
    datadog/connector:
exporters:
  datadog:
    api:
      key: 9842ecdxxxxxxxxxxxxxxxxxxxxxxx
service:
  telemetry:
    logs:
      level: info
  pipelines:
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog]
    logs:
      receivers: [filelog]
      processors: [batch]
      exporters: [datadog]
```

The provided configuration snippet for the OpenTelemetry Collector showcases the setup for receiving logs from files, processing them, and exporting to a backend, specifically Datadog in this instance. Here's a detailed description of the key components related to the `filelog` receiver:

### Receivers

- **`filelog`**: This receiver is configured to monitor and read logs from the file specified in the `include` parameter. In this case, it's set to `/var/log/test/simple.log`. The `filelog` receiver will watch this file for new log entries and process them as they appear.

### Operators (within `filelog` receiver)

- **`regex_parser`**: This operator is used to parse the incoming log lines using a regular expression defined in the `regex` parameter. The pattern `^(?P<time>\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) (?P<sev>[A-Z]*) (?P<msg>.*)$` is designed to extract timestamps, severity levels, and the message content from each log entry.


### Processors

- **`batch`**: This processor is configured to batch together log data before sending it to the exporter. The `timeout` parameter is set to `10s`, indicating that logs will be batched for up to 10 seconds before being sent out. This helps improve throughput and reduce network overhead.

### Exporters

- **`datadog`**: Configured to send the processed log data to Datadog.

### Service

The `service` section defines the pipelines for metrics, traces, and logs, specifying which receivers, processors, and exporters are included in each pipeline. In this case:

- The `logs` pipeline is configured to use the `filelog` receiver, the `batch` processor, and the `datadog` exporter. This setup ensures that logs collected from the specified file are processed and then exported to Datadog for monitoring and analysis.


## Unified service tagging

Unified Service Tagging is a methodology recommended by Datadog to ensure a consistent tagging strategy across all the data types (metrics, traces, logs) collected and observed within their platform. This approach facilitates efficient organization, filtering, and correlation of data across the different components of your environment. 

Here's a closer look at its key aspects:

### Key Components

- **Environment (`env`)**: This tag distinguishes data originating from different stages of your development lifecycle, such as production (`prod`), staging (`stage`), or development (`dev`). It's helpful for separating and filtering data based on the environment, allowing for targeted analysis and alerts.

- **Service (`service`)**: The service tag identifies the specific service or application generating the data. 

- **Version (`version`)**: This tag is used to specify the version of the service or application, enabling you to compare metrics, logs, and traces across different releases. This is particularly useful for tracking down errors introduced in new deployments or observing improvements in performance over time.

### Benefits

1. **Correlation**: By using unified tags, Datadog enables you to correlate metrics, traces, and logs effortlessly. This means you can trace a problematic request from a high-level dashboard down to the specific logs and traces that detail the issue, all using common identifiers.

2. **Improved Navigation**: It simplifies navigating Datadog's UI, as data can be filtered and searched based on standardized tags. This improves the efficiency of diagnosing problems and understanding system behavior.

3. **Automated Alerting**: With consistent tagging, setting up alerts based on specific criteria across your entire stack becomes much simpler. For example, you could set an alert for error rates on a particular service in the production environment, ensuring you're immediately notified of potential issues.

4. **Better Reporting**: Unified Service Tagging aids in generating more meaningful and contextual reports. You can compare services, versions, or environments to gain insights into the overall health and performance of your applications.

### Adjusting the collector configuration file

In order to have unified service tagging configured, we will modify the processor by adding the `attributes` referring to `env`, `service` and optionally `version`.  

```yaml
receivers:
  otlp:
    protocols:
      http:
      grpc:
  filelog:
    include: [ /var/log/test/simple.log ]
    operators:
      - type: regex_parser
        regex: '^(?P<time>\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) (?P<sev>[A-Z]*) (?P<msg>.*)$'
        timestamp:
          parse_from: attributes.time
          layout: '%Y-%m-%d %H:%M:%S'
        severity:
          parse_from: attributes.sev      
processors:
  batch:
    timeout: 10s
  attributes:
    actions:
      - key: host
        value: "pt-instance-1"
        action: upsert
      - key: service
        value: "springotel"
        action: upsert
      - key: ddsource
        value: "java"
        action: upsert
      - key: env
        value: "dev"
        action: upsert
connectors:
    datadog/connector:
exporters:
  datadog:
    api:
      key: 98xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
service:
  telemetry:
    logs:
      level: info
  pipelines:
    metrics:
      receivers: [otlp]
      processors: [batch, attributes]
      exporters: [datadog]
    traces:
      receivers: [otlp]
      processors: [batch, attributes]
      exporters: [datadog]
    logs:
      receivers: [filelog]
      processors: [batch, attributes]
      exporters: [datadog]
```

The `attributes` section in the OpenTelemetry Collector configuration file is part of the processing phase that manipulates or adds attributes (metadata) to telemetry metrics, traces, and logs (before it is exported to a backend system, in this case, Datadog). This processor can modify the attributes of data passing through the collector, allowing for richer, more meaningful data to be sent to the backend. Here's a breakdown of how it works based on the provided configuration:

### Configuration Details

In the provided configuration, the `attributes` processor is defined with several actions, each specifying how to manipulate the data attributes:

1. **Upsert Actions**: Each action defined under `actions` performs an "upsert" operation on the attributes. "Upsert" means to update the attribute if it exists or insert it if it does not. This ensures that the specified key-value pairs are present in the telemetry data.

2. **Key-Value Pairs**: The key-value pairs specified in the actions are as follows:
   - `host`: Set to "pt-instance-1". This attribute specifies the host or instance from which the telemetry data is collected.
   - `service`: Set to "springotel". This denotes the service name, allowing for the aggregation and filtering of data by service in the backend.
   - `ddsource`: Set to "java". This attribute indicates the source of the logs or telemetry data, which will enable the Datadog log pipeline for java. When you set the `ddsource` to a specific value, such as "java", it instructs Datadog to apply a specific set of preset processing rules or a pipeline that is tailored to logs coming from that type of application.
   - `env`: Set to "dev". This specifies the environment (development, in this case) from which the telemetry data is originating, aiding in environment-specific analysis and alerting.

3. **Pipeline section**:

- The `attributes` processor is applied to both the `metrics`, `traces` and `logs` pipelines, as indicated in the `service` section under `pipelines`. This ensures that the metrics, trace and log data processed by the collector includes the defined attributes before being exported to Datadog.


## Enabling logging in our Spring Boot application 

WIP

## Build, run and test the application

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section10/activity]$ gradle build

BUILD SUCCESSFUL in 4s
4 actionable tasks: 4 executed

[root@pt-instance-1:~/oteljavalab/section09/activity]$ java -jar build/libs/springotel-0.0.1-SNAPSHOT.jar &
2024-03-02T12:11:25.450Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 30923 (/root/oteljavalab/section10/activity/build/libs/springotel-0.0.1-SNAPSHOT.jar started by root in /root/oteljavalab/section10/activity)
2024-03-02T12:11:25.484Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : No active profile set, falling back to 1 default profile: "default"
2024-03-02T12:11:27.116Z  INFO 30923 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-03-02T12:11:27.133Z  INFO 30923 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-03-02T12:11:27.134Z  INFO 30923 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.18]
2024-03-02T12:11:27.189Z  INFO 30923 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-03-02T12:11:27.193Z  INFO 30923 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1561 ms
2024-03-02T12:11:28.023Z  INFO 30923 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-02T12:11:28.051Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : Started TemperatureApplication in 3.372 seconds (process running for 4.028)

</pre>

Generate several requests from another terminal using curl (or from a browser or postman)

<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab/section10/activity]$ for i in {1..5}; do "localhost:8080/simulateTemperature?measurements=5&location=Paris"; sleep 1; done

[21,28,29,35,27]
[24,32,29,33,32]
...
[28,21,24,22,23]
</pre>


## Check the results in the Datadog UI (Log search)

After having run a few requests, you should be able to see the corresponding log events in the `Log search` by searching it by its attributes


WIP


## End



