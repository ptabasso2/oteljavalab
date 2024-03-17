# Sending other Observability signals: Metrics 


## Goal of this activity

After having discussed various aspects of tracing, we are moving to another cornerstone of observability: Metrics. Metrics offer quantifiable data that reflect the state, performance, and behavior of our applications and infrastructure. They are key for monitoring health indicators like throughput, latency, error rates, and resource utilization over time.

This new section will guide you through the process of capturing and sending metrics using the OpenTelemetry SDK. Metrics in OpenTelemetry are time-series data, which means they are measurements captured over intervals, providing a stream of data points that represent changes in the system.

OpenTelemetry provides a robust set of tools to capture everything from simple counters to complex measures. The Otel SDK abstracts away the complexities of metric collection, allowing developers to focus on what to measure rather than how to measure.


## Main steps

In section 4, we explored how to set attributes through manual instrumentation. In this section we will start from where we were at the end of that chapter and focus on following the steps using the Java SDK to create and send metrics. 

- Configure the SDK
- Create the metric in the Thermometer class
- Build, run, test
- Visualize the metric in the Metrics Explorer 


## Configure the SDK to send metrics

**bootsrap the containers**

(Make sure the `DD_API_KEY` and `DD_SITE` env variables are set)   

```bash
[root@pt-instance-1:~/oteljavalab]$ DD_SITE="your_site_value" DD_API_KEY="your_api_key_value" docker-compose up -d
Creating otel-collector ... done
Creating springotel     ... done
```

**Accessing the container**

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ docker exec -it springotel bash
[root@pt-instance-1:~/oteljavalab]$ 
</pre>

**Navigate to the project directory**

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ cd section09/activity
[root@pt-instance-1:~/oteljavalab/section09/activity]$
</pre>

<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab/section09/activity]$ ll src/main/java/com/pej/otel/springotellab/
total 20
drwxr-xr-x 2 root root 4096 Mar  6 15:42 ./
drwxr-xr-x 3 root root 4096 Mar  3 10:09 ../
-rw-r--r-- 1 root root 1617 Mar  3 12:53 TemperatureApplication.java
-rw-r--r-- 1 root root 2151 Mar  3 12:55 TemperatureController.java
-rw-r--r-- 1 root root 1687 Mar  3 13:04 Thermometer.java
</pre>


### SDK configuration


In order to create metrics we need to provide an additional dependency to our project:

`implementation("io.opentelemetry:opentelemetry-sdk-metrics:1.35.0")`

(in the dependency block of the `build.gradle.kts` file)


```java
dependencies {
        compile("org.springframework.boot:spring-boot-starter-web")
        implementation("io.opentelemetry:opentelemetry-api")
        implementation("io.opentelemetry:opentelemetry-sdk")
        implementation("io.opentelemetry:opentelemetry-exporter-logging")
        implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.23.1-alpha")
        implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.35.0")
        implementation("io.opentelemetry:opentelemetry-sdk-metrics:1.35.0")


}
```

***Initial Tracing-Only Configuration***

Then we will focus on the configuration block of the SDK which is located in the `ThermometerApplication` class. 


```java
    @Bean
    public OpenTelemetry openTelemetry(){

        Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "springotel").build();

        OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder().setTimeout(2, TimeUnit.SECONDS).build();

        SdkTracerProvider setTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).setScheduleDelay(100, TimeUnit.MILLISECONDS).build())
                .setResource(resource)
                .build();

        return OpenTelemetrySdk.builder().setTracerProvider(setTracerProvider).buildAndRegisterGlobal();

    }
```


In the original code snippet we set up OpenTelemetry for tracing only:

- **Resource**: A `Resource` object is created with the `SERVICE_NAME` attribute, which is used to identify the service in the telemetry data.
- **Span exporter**: An `OtlpGrpcSpanExporter` is instantiated for exporting spans (trace data) over gRPC to an observability backend that supports the OTLP (OpenTelemetry Protocol).
- **Tracer provider**: A `SdkTracerProvider` is built and configured with a `BatchSpanProcessor`, which batches and sends spans asynchronously to the exporter. The `Resource` is also associated with the tracer provider.
- **OpenTelemetry SDK initialization**: Finally, the `OpenTelemetrySdk` is built with the configured tracer provider and is registered globally. This means that anywhere in the application where tracing is initiated, this configuration will be used.

***Enhanced Configuration for Both Tracing and Metrics***

The code change to add metrics collection is as follows:

```java
    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, "springotel")
                .build();

        OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                .setTimeout(2, TimeUnit.SECONDS)
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).setScheduleDelay(100, TimeUnit.MILLISECONDS).build())
                .setResource(resource)
                .build();

        OtlpGrpcMetricExporter otlpGrpcMetricExporter = OtlpGrpcMetricExporter.builder()
                .setTimeout(2, TimeUnit.SECONDS)
                .build();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(otlpGrpcMetricExporter).build())
                .setResource(resource)
                .build();


        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .buildAndRegisterGlobal();
    }
```

This will require the following imports:

```java
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
```


Metrics collection is enabled by providing these additional elements:

- **Metric exporter**: An `OtlpGrpcMetricExporter` is set up in a similar fashion to the span exporter, but for exporting metric data over gRPC.
- **Meter provider**: A `SdkMeterProvider` is built to handle metric instruments and their recordings. It's configured with a `PeriodicMetricReader` that uses the `OtlpGrpcMetricExporter`. This reader periodically collects and exports metrics using the configured exporter. Like the tracer provider, the meter provider is also associated with the `Resource`.
- **OpenTelemetry SDK initialization for metrics and traces**: The `OpenTelemetrySdk` is built with both the tracer provider and the meter provider. By setting up both providers, the OpenTelemetry SDK now supports the collection and export of both traces and metrics, providing a comprehensive observability solution.


## Creating the Metric

The second step consists of adding the actual code for creating the metric.

Creating a metric involves defining what type of metric to record, configuring it, and then incrementing or updating the metric as part of the application's logic. In the provided `Thermometer` class, we create a metric to count the number of temperature measurements taken. Here are the steps for creating this metric:

### 1. **Define the Meter**:

A `Meter` instance is obtained from `OpenTelemetry` which acts as a factory for creating various kinds of metric instruments. Here, the `Meter` is named "TemperatureMeter", which could represent a logical grouping of temperature-related metrics.

```java
meter = openTelemetry.getMeter("TemperatureMeter");
```

### 2. **Create a Counter**:

A counter is a cumulative metric that represents a single numerical value that only increases. The `LongCounter` is an implementation of a counter that deals with integer values.

```java
this.temperatureMeasurementsCounter = meter.counterBuilder("temperature_measurements")
        .setDescription("Counts the number of temperature measurements made")
        .setUnit("1")
        .build();
```

In this snippet:

- `counterBuilder("temperature_measurements")`: This defines a new counter and names it "temperature_measurements".
- `.setDescription(...)`: Sets a human-readable description for the metric.
- `.setUnit("1")`: Sets the unit of the metric. Here, "1" indicates that the metric is a count with no specific unit.
- `.build()`: Finalizes the counter creation.


After the modification our code should now look like this:

```java
@Component
public class Thermometer {

    private int minTemp;
    private int maxTemp;

    private final Tracer tracer;

    private final Meter meter;

    private LongCounter temperatureMeasurementsCounter;

    @Autowired
    Thermometer(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(Thermometer.class.getName(), "0.1.0");
        meter = openTelemetry.getMeter("TemperatureMeter");
        this.temperatureMeasurementsCounter  = meter.counterBuilder("temperature_measurements")
                .setDescription("Counts the number of temperature measurements made")
                .setUnit("1")
                .build();
    }

    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();
        Span parentSpan = tracer.spanBuilder("simulateTemperature").startSpan();
        try (Scope scope = parentSpan.makeCurrent()){
            for (int i = 0; i < measurements; i++) {
                temperatures.add(this.measureOnce());
                temperatureMeasurementsCounter.add(1);
            }
            return temperatures;
        } finally {
            parentSpan.end();
        }
    }

    private int measureOnce() {
        Span childSpan = tracer.spanBuilder("measureOnce").startSpan();
        try {
            return ThreadLocalRandom.current().nextInt(this.minTemp, this.maxTemp + 1);
        } finally {
            childSpan.end();
        }
    }

    public void setTemp(int minTemp, int maxTemp){
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

}

```

This would require the following imports

```java
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
```

### 3. **Record Measurements**:

Each time a temperature measurement is taken within the `simulateTemperature` method, the counter is incremented by 1.

```java
temperatureMeasurementsCounter.add(1);
```

This line of code effectively counts the number of temperature readings by incrementing the counter each time the `measureOnce` method is called.

The metric creation steps are straightforward and easy to replicate. This requires following the process of instantiate a meter, create a counter from that meter, and then use that counter within the application to record data. In the context of the `Thermometer` class, every call to `simulateTemperature` will result in incrementing the counter, providing a running total of all temperature measurements made during the application's lifetime. 


## Build, run and test the application

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section09/activity]$ gradle build

BUILD SUCCESSFUL in 4s
4 actionable tasks: 4 executed

[root@pt-instance-1:~/oteljavalab/section09/activity]$ java -jar build/libs/springotel-0.0.1-SNAPSHOT.jar &
2024-03-02T12:11:25.450Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 30923 (/root/oteljavalab/section09/activity/build/libs/springotel-0.0.1-SNAPSHOT.jar started by root in /root/oteljavalab/section09/activity)
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

[root@pt-instance-1:~/oteljavalab/section09/activity]$ for i in {1..25}; do "localhost:8080/simulateTemperature?measurements=5&location=Paris"; sleep 1; done

[21,28,29,35,27]
[24,32,29,33,32]
...
[28,21,24,22,23]
</pre>


## Check the results in the Datadog UI (APM traces)

After having run the requests (consider running at least 10/20 requests to have enough datapoints), you should be able to see the corresponding metric from the `Metrics Explorer` by searching it by its name (`temperature_measurements`)


<p align="left">
  <img src="img/springotel90.png" width="850" />
</p>

## Tearing down the services

Exit the container

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section09/activity]$ exit
[root@pt-instance-1:~/oteljavalab/section09/activity]$ 
</pre>

Graceful shutdown

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section09/activity]$ docker-compose down
Stopping otel-collector   ... done
Stopping springotel       ... done
</pre>


## End



