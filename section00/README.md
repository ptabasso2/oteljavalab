
# OpenTelemetry collector contrib installation guide

This guide provides detailed steps to install the OpenTelemetry Collector Contrib on an Ubuntu host.

## Prerequisites
- Ubuntu system with sudo privileges or Docker 
- Internet connection.


## Installation steps if using Docker

Once you have cloned the repository, copy or rename the file named `collector-template.yaml` to `collector.yaml` located under the `./oteljavalab/section00/activity` directory.

```bash
[root@pt-instance-1:~/oteljavalab]$ cp section00/activity/collector-template.yaml section00/activity/collector.yaml
```


Edit it and add the required configuration details (`datadog.api.site`, `datadog.api.key`) 


```ìni
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
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog]
```

**Note**: By default the datadog site will be set to `datadoghq.com`. If you wish to target any other backend (ex for EU or US3, US4 etc...), you will want to set the site to the corresponding value. Ex for Europe, `site: datadoghq.eu` 


Save the file and spin up the containers

You would only need to run the following command that starts two containers one running the collector, the other one running the application container.

```bash
[root@pt-instance-1:~/oteljavalab]$ docker-compose up -d
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

## Installation steps if running locally

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

```ìni
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
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog]
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [datadog]
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



## Conclusion
Follow these steps to install and configure the OpenTelemetry Collector Contrib on your Ubuntu host.
