
# OpenTelemetry collector contrib installation guide

This guide provides detailed steps to install the OpenTelemetry collector contrib.

The OpenTelemetry collector is a core component of the OpenTelemetry observability framework, designed to collect, process, and export telemetry data such as metrics, traces, and logs. 

<p align="left">
  <img src="img/springotel01.png" width="650" />
</p>

It's a vendor-agnostic, high-performance application that can run as a standalone service or be embedded into other applications. Its primary purpose is to simplify the collection and management of observability data, especially in complex, distributed systems. Here's a breakdown of its key functions and features:

### Collector
The collector can receive telemetry data from various sources. It supports receiving data over multiple protocols and formats, capable of serving as a central aggregation point for data from applications instrumented with different observability tools.

### Processing
Once telemetry data is collected, the collector can process this data before exporting. Processing capabilities include:
- **Transformation**: Modifying data attributes or structure, such as renaming attributes or transforming metric data points.
- **Batching**: Combining multiple data points into batches to improve export efficiency.
- **Filtering**: Dropping or selecting specific data points based on defined criteria.
- **Sampling**: Reducing the volume of trace data by selectively capturing only a portion of the traces.

These processing capabilities allow for significant flexibility in managing telemetry data, enabling optimizations for performance and cost, or tailoring the data for specific backend requirements.

### Exporting
The collector can export processed data to one or more observability backends or other analysis tools. It supports a wide range of exporters for popular monitoring, tracing, and logging platforms, including but not limited to:
- Cloud-native observability platforms (e.g., AWS CloudWatch, Google Cloud Monitoring, Azure Monitor)
- Open-source monitoring and tracing tools (e.g., Prometheus, Jaeger, Grafana)
- Third-party observability services (e.g., Datadog, New Relic, Splunk)

This export functionality is configurable, allowing data to be sent to multiple destinations simultaneously, which is useful for routing data to different systems for different purposes (e.g., real-time monitoring, long-term storage, detailed trace analysis).

### Deployment flexibility
The collector can be deployed in several modes to fit various architecture needs:
- **Agent**: Running alongside each host or in each container to collect data locally.
- **Gateway**: Running as a standalone service to aggregate and process data from multiple sources before exporting to backend systems.

### Benefits
- **Simplification of instrumentation**: By acting as a unified receiver for telemetry data, it simplifies the instrumentation required within applications and services.
- **Vendor agnostic**: Its ability to receive and export data in multiple formats to various backends allows for a flexible observability strategy that is not locked into any specific vendor.
- **Scalability and reliability**: Designed for high-volume and high-availability deployments, it can be scaled out and configured for redundancy.

Overall, the Otel collector plays a critical role in the observability ecosystem by providing a centralized, efficient, and flexible solution for telemetry data collection, processing, and exporting. Its design addresses the challenges of managing observability data at scale, making it easier for organizations to implement comprehensive monitoring and tracing strategies.


## Prerequisites
- Docker 
- Internet connection.


## Installation steps if using Docker

Once you have cloned the repository, the collector config file named `collector.yaml` (already present under the `./oteljavalab/section00/activity` directory) will be used in the following sections.


You would need to set the two environment variables DD_SITE and DD_API_KEY with their respective values:
For the datadog site specifically it should be set to `datadoghq.com` unless you have access to another site (ex EU or US3, US4 etc...), you will want to set the site to the corresponding value. Ex for Europe, `site: datadoghq.eu`

For the detailed list of sites:

[Site list](https://docs.datadoghq.com/getting_started/site/#access-the-datadog-site)


```yaml
receivers:
  otlp:
    protocols:
      http:
      grpc:
processors:
  batch:
    timeout: 10s
connectors:
    datadog/connector:
exporters:
  datadog:
    api:
      site: ${DD_SITE}
      key: ${DD_API_KEY}
service:
  telemetry:
    logs:
      level: info
  pipelines:
    metrics:
      receivers: [datadog/connector, otlp]
      processors: [batch]
      exporters: [datadog]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog/connector, datadog]
```

### Description:

1. **Receivers**: These are components responsible for receiving telemetry data. In this configuration, there are two types of receivers defined:
   - **OTLP (OpenTelemetry Protocol)**: This receiver is configured to use two protocols: HTTP and gRPC.
   
2. **Processors**: Processors are responsible for manipulating or transforming the incoming data. In this configuration, there's one processor defined:
   - **Batch**: This processor batches incoming telemetry data with a timeout of 10 seconds.

3. **Connectors**: Connectors bridge the service with external systems or services. In this configuration, there's one connector defined:
   - **Datadog Connector**: The Datadog Connector is responsible for computing stats and APM metrics tied to the traces it receives and then exports them. These then go through the metrics pipeline which has the Datadog connector as a receiver. The Datadog exporter then translates them into stats payload that gets sent to the platform. 

4. **Exporters**: Exporters are responsible for sending telemetry data to external systems or services. In this configuration, there's one exporter defined:
   - **Datadog Exporter**: This exporter is configured to send telemetry data to Datadog. It requires API credentials, which are specified using environment variables (${DD_SITE} and ${DD_API_KEY}).

5. **Service**: This section specifies configurations related to the telemetry service itself:
   - **Telemetry**: Defines telemetry-related configurations.
     - **Logs**: Configures log-related settings, setting the log level to "info".
   - **Pipelines**: Defines different pipelines for processing telemetry data.
     - **Metrics**: Defines a pipeline for handling metrics data. It specifies which receivers, processors, and exporters to use.
     - **Traces**: Defines a pipeline for handling trace data. It specifies which receivers, processors, and exporters to use.




You would only need to run the following command that starts two containers one running the collector, the other one running the application container. 
Make sure to execute de command from the project root directory 

```bash
[root@pt-instance-1:~/oteljavalab]$ DD_SITE="your_site_value" DD_API_KEY="your_api_key_value" docker-compose up -d
Creating otel-collector ... done
Creating springotel     ... done
```


You should be able to see the two containers up and running

```bash
[root@pt-instance-1:~/oteljavalab]$ docker-compose ps
     Name                   Command               State   Ports
---------------------------------------------------------------
otel-collector   /otelcol-contrib --config  ...   Up           
springotel       /__cacert_entrypoint.sh sl ...   Up           
```

You may go to the next section (`section01`) about the application details.


## (Optional) Installation steps if running locally without Docker

Note: In the remaining instructions, all steps will be performed as the root user. 

### Step 1: Update the system
Update your system packages.

```bash
[root@pt-instance-1:~]$ apt update
[root@pt-instance-1:~]$ apt upgrade
```

### Step 2: Install required dependencies
Install necessary dependencies.

```bash
[root@pt-instance-1:~]$ apt install wget tar
```

### Step 3: Create installation directory
Create the directory for the OpenTelemetry Collector.

```bash
[root@pt-instance-1:~]$ mkdir -p /root/otelcollector
```

### Step 4: Download OpenTelemetry collector contrib
Download the OpenTelemetry Collector Contrib.

```bash
[root@pt-instance-1:~]$ cd /root/otelcollector
[root@pt-instance-1:~/otelcollector]$ wget https://github.com/open-telemetry/opentelemetry-collector-contrib/releases/download/<version>/otelcol-contrib_<version>_linux_amd64.tar.gz
```

### Step 5: Extract the collector
Extract the downloaded file.

```bash
[root@pt-instance-1:~/otelcollector]$ tar -xvzf otelcol-contrib_<version>_linux_amd64.tar.gz
```

### Step 6: Configure the collector
Create a configuration file.

```bash
[root@pt-instance-1:~/otelcollector]$ vi otel-collector-config.yaml
```

Add your configuration as follows:

```yaml
receivers:
  otlp:
    protocols:
      http:
      grpc:
processors:
  batch:
    timeout: 10s
connectors:
    datadog/connector:
exporters:
  datadog:
    api:
      site: <your datadog api site (default is datadoghq.com)>
      key: <your datadog api key>
service:
  telemetry:
    logs:
      level: info
  pipelines:
    metrics:
      receivers: [datadog/connector, otlp]
      processors: [batch]
      exporters: [datadog]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog/connector, datadog]
```

**Note**: By default the datadog site will be set to datadoghq.com. If you wish to target any other backend (ex for EU or US3, US4 etc...), you will want to set the site to the corresponding value. Ex for Europe, `site: datadoghq.eu` 

### Step 7: Start the collector
Run the Collector with the configuration.

```bash
[root@pt-instance-1:~/otelcollector]$ ./otelcol-contrib --config otel-collector-config.yaml
```

You should get a similar output

```bash
2024-01-28T12:00:00.123Z info    service/collector.go:303  Starting otelcol-contrib...     {"Version": "v0.x.x", "GitHash": "abc1234", "NumCPU": 4}
2024-01-28T12:00:00.456Z info    service/collector.go:242  Loading configuration...
2024-01-28T12:00:00.789Z info    service/collector.go:258  Applying configuration...
2024-01-28T12:00:01.012Z info    service/telemetry.go:98   Setting up own telemetry...
2024-01-28T12:00:01.345Z info    service/telemetry.go:116  Serving Prometheus metrics      {"address": ":8888", "level": 0, "service.instance.id": "12345-6789-abcdef"}
2024-01-28T12:00:01.678Z info    service/collector.go:342  Everything is ready. Begin running and processing data.
2024-01-28T12:00:01.901Z info    service/collector.go:206  Starting receiver...            {"kind": "receiver", "name": "otlp"}
2024-01-28T12:00:02.234Z info    service/collector.go:206  Starting exporter...            {"kind": "exporter", "name": "logging"}
2024-01-28T12:00:02.567Z info    service/collector.go:206  Starting processor...           {"kind": "processor", "name": "batch"}
2024-01-28T12:00:02.890Z info    service/collector.go:206  Starting extension...           {"kind": "extension", "name": "health_check"}
2024-01-28T12:00:03.123Z info    service/collector.go:252  Started extension               {"kind": "extension", "name": "health_check"}
2024-01-28T12:00:03.456Z info    builder/pipelines_builder.go:204  Pipeline is started.    {"kind": "pipeline", "name": "traces"}
2024-01-28T12:00:03.789Z info    builder/pipelines_builder.go:204  Pipeline is started.    {"kind": "pipeline", "name": "metrics"}
2024-01-28T12:00:04.012Z info    healthcheck/handler.go:129 Health Check state change      {"kind": "extension", "name": "health_check", "status": "ready"}
2024-01-28T12:00:04.345Z info    service/collector.go:267  Started receiver               {"kind": "receiver", "name": "otlp"}
2024-01-28T12:00:04.678Z info    service/collector.go:267  Started exporter               {"kind": "exporter", "name": "logging"}
2024-01-28T12:00:05.001Z info    service/collector.go:267  Started processor              {"kind": "processor", "name": "batch"}
2024-01-28T12:00:05.324Z info    service/collector.go:190  Started components.            {"kind": "service"}
2024-01-28T12:00:05.647Z info    service/collector.go:392  Starting Cmd.                  {"kind": "service"}
```


### Step 8: Verify the installation
Check if the Collector is running.

```bash
[root@pt-instance-1:~/otelcollector]$ ps -ef | grep otelcol-contrib
```


### Step 9: Setting up as a system service (Optional)
Set up the collector as a system service.

1. Create a systemd service file.

    ```bash
    [root@pt-instance-1:~/otelcollector]$ vi /etc/systemd/system/otelcol-contrib.service
    ```

2. Add the service configuration.

    ```ini
    [Unit]
    Description=OpenTelemetry Collector Contrib
    After=network.target

    [Service]
    User=root
    ExecStart=/root/otelcollector/otelcol-contrib --config=/root/otelcollector/otel-collector-config.yaml
    Restart=on-failure

    [Install]
    WantedBy=multi-user.target
    ```

3. Enable and start the service.

    ```bash
    [root@pt-instance-1:~/otelcollector]$ systemctl enable otelcol-contrib
    [root@pt-instance-1:~/otelcollector]$ systemctl start otelcol-contrib
    ```

4. Check the service status.

    ```bash
    [root@pt-instance-1:~/otelcollector]$ systemctl status otelcol-contrib
    ```

### Step 10: Confirm functionality
In the following sections, we will confirm this after generating some traces and metrics.


## End