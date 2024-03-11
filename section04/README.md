# Manual tracing: How to set span attributes



## Goal of this activity

In this section of the OpenTelemetry tutorial, we will explore how to programmatically set or modify span attributes using the Java SDK. You'll learn to enrich your telemetry data by adding key-value pairs to spans, which can provide more context about the operation being traced. We'll cover how to use the `setAttribute` method to dynamically adjust span information, such as adding custom attributes or updating existing ones, enhancing the observability of your applications.

## Main steps

In the previous section we explored how to perform basic tasks related to manual instrumentation and span creation. In this section we will start from where we were at the end of the previous chapter and focus on setting or changing span attributes programmatically using the Java SDK in OpenTelemetry. 


## Setting span attributes

**Accessing the container first**

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ docker exec -it springotel bash
[root@pt-instance-1:~/oteljavalab]$ 
</pre>

**Navigate to the project directory**

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ cd section04/activity
[root@pt-instance-1:~/oteljavalab/section04/activity]$
</pre>

<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab/section04/activity]$ ll src/main/java/com/pej/otel/springotellab/
total 20
drwxr-xr-x 2 root root 4096 Mar  6 15:42 ./
drwxr-xr-x 3 root root 4096 Mar  3 10:09 ../
-rw-r--r-- 1 root root 1617 Mar  3 12:53 TemperatureApplication.java
-rw-r--r-- 1 root root 2151 Mar  3 12:55 TemperatureController.java
-rw-r--r-- 1 root root 1687 Mar  3 13:04 Thermometer.java
</pre>



Using the `setAttribute` method is done as follows:
In this example we will define a span type as `web` so that it will get rendered in the Datadog UI as a web component and specify the resource name which happens to be the name of the endpoint exposed by the spring controller (`/simulateTemperature`).


Here is how we do it practically:

We need to edit the `TemperatureController.java` file and adapt the line where we create the span by appending the `setAttribute` method calls:

`setAttribute("span.type", "web")` and `setAttribute("resource.name", "GET /simulateTemperature")`

```java
        Span span = tracer.spanBuilder("temperatureSimulation").startSpan();
        span.setAttribute("span.type", "web");
        span.setAttribute("resource.name", "GET /simulateTemperature");
        try (Scope scope = span.makeCurrent()) {

            if (measurements.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing measurements parameter", null);
            }

            thermometer.setTemp(20, 35);
            List<Integer> result = thermometer.simulateTemperature(measurements.get());

            if (location.isPresent()) {
                logger.info("Temperature simulation for {}: {}", location.get(), result);
            } else {
                logger.info("Temperature simulation for an unspecified location: {}", result);
            }
            return result;
        } catch(Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    
```

## Setting semantic attributes in spans

Let's connect to the application container, ensure you're in the correct working directory.

```bash
[root@pt-instance-1:~/oteljavalab]$ docker exec -it springotel bash
[root@pt-instance-1:~/oteljavalab]$ 
```

**Navigate to the project directory**

First, change into the directory containing your Spring Boot project:

```bash
[root@pt-instance-1:~/oteljavalab]$ cd section04/activity
[root@pt-instance-1:~/oteljavalab/section04/activity]$
```

**Examine the project's java source files**

Review the existing Java source files in your project:

```bash
[root@pt-instance-1:~/oteljavalab/section04/activity]$ ll src/main/java/com/pej/otel/springotellab/
total 20
drwxr-xr-x 2 root root 4096 Mar  6 15:42 ./
drwxr-xr-x 3 root root 4096 Mar  3 10:09 ../
-rw-r--r-- 1 root root 1617 Mar  3 12:53 TemperatureApplication.java
-rw-r--r-- 1 root root 2151 Mar  3 12:55 TemperatureController.java
-rw-r--r-- 1 root root 1687 Mar  3 13:04 Thermometer.java
```

**Adding semantic attributes to spans**

Semantic attributes allow you to annotate spans with a standardized set of attributes that provide context about the nature of the operation being traced. For instance, when tracing web requests, you might want to include attributes like HTTP method, URL path, and status code. These attributes make it easier to filter and analyze trace data in observability platforms like Datadog.

Let's add semantic attributes to a span in the `TemperatureController.java` to include details about an HTTP request:

```java

// Inside the TemperatureController's method where the span is created
Span span = tracer.spanBuilder("temperatureSimulation").startSpan();
span.setAttribute(SemanticAttributes.HTTP_METHOD, "GET /simulateTemperature");
span.setAttribute(SemanticAttributes.HTTP_URL, "http://example.com/simulateTemperature");
span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, 200);

try (Scope scope = span.makeCurrent()) {

    if (measurements.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing measurements parameter", null);
    }

    thermometer.setTemp(20, 35);
    List<Integer> result = thermometer.simulateTemperature(measurements.get());

    if (location.isPresent()) {
        logger.info("Temperature simulation for {}: {}", location.get(), result);
    } else {
        logger.info("Temperature simulation for an unspecified location: {}", result);
    }
    return result;
} catch(Throwable t) {
    span.recordException(t);
    throw t;
} finally {
    span.end();
}
```

This requires importing the following package:

`import io.opentelemetry.semconv.SemanticAttributes;`


**Key points**

- `SemanticAttributes.HTTP_METHOD`, `SemanticAttributes.HTTP_URL`, and `SemanticAttributes.HTTP_STATUS_CODE` are examples of semantic attributes provided by OpenTelemetry to standardize the way common details about operations (like HTTP requests) are described.
- By annotating spans with semantic attributes, you can enhance the observability of your application, making it easier to query and understand trace data.
- Ensure that you import the `SemanticAttributes` class from the OpenTelemetry API to use these predefined attribute keys.

By following this example, you integrate semantic attributes into your application's spans, thereby enriching the telemetry data collected during the execution of HTTP requests.


## Observations

Using span attributes effectively is intrinsically linked to adhering to semantic conventions within the context of distributed tracing, especially when using OpenTelemetry. Semantic conventions are a set of guidelines and standardized attribute names that provide a uniform way to describe the nature of the span, the work it represents, and additional context about the operation being traced. These conventions facilitate consistency across different services and applications, enabling more straightforward analysis and monitoring across a distributed system.

Semantic conventions cover various aspects, including but not limited to:

- **Service identification**: Attributes that identify the service instance, such as `service.name`, and `service.version`, help in distinguishing between different services and versions within a traced system.
- **Operation details**: Standardized names for operations, such as HTTP method (`http.method`) and URL (`http.url`), database type (`db.system`), messaging system operation (`messaging.operation`), etc., provide clear, consistent descriptions of what a span represents.
- **Error handling**: Using attributes like `exception.type` and `exception.message` to record error information in a standardized format makes it easier to identify and analyze failures across different services and languages.
- **Network and infrastructure**: Attributes describing network operations (e.g., `net.peer.ip`, `net.peer.port`) and infrastructure details (e.g., `cloud.provider`, `container.id`) offer insights into the underlying infrastructure and network context of operations.

Adhering to these conventions when using span attributes ensures that telemetry data from various sources can be integrated and analyzed cohesively. It allows developers and SREs to query and visualize traces and metrics in a consistent manner across tools and platforms, such as Datadog, Prometheus, Grafana, etc.

For example, when you set a span attribute to categorize a span as a web component using `"span.type": "web"` or specify a resource name with `"resource.name"`, you are leveraging a form of semantic convention. Although `"span.type"` and `"resource.name"` in the given example might not strictly follow the OpenTelemetry semantic conventions, the principle is to use meaningful, standardized attributes to describe the span's context and purpose. The actual OpenTelemetry semantic conventions would use attributes like `http.method` and `http.route` to provide similar types of information for HTTP operations.

In essence, coupling span attributes with semantic conventions enhances the observability and operational insights of distributed systems by ensuring data consistency and interpretability across different telemetry sources and tools.

## Build, run and test the application

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section04/activity]$ gradle build

BUILD SUCCESSFUL in 4s
4 actionable tasks: 4 executed

[root@pt-instance-1:~/oteljavalab/section04/activity]$ java -jar build/libs/springotel-0.0.1-SNAPSHOT.jar &
2024-03-02T12:11:25.450Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 30923 (/root/oteljavalab/section04/activity/build/libs/springotel-0.0.1-SNAPSHOT.jar started by root in /root/oteljavalab/section04/activity)
2024-03-02T12:11:25.484Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : No active profile set, falling back to 1 default profile: "default"
2024-03-02T12:11:27.116Z  INFO 30923 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-03-02T12:11:27.133Z  INFO 30923 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-03-02T12:11:27.134Z  INFO 30923 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.18]
2024-03-02T12:11:27.189Z  INFO 30923 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-03-02T12:11:27.193Z  INFO 30923 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1561 ms
2024-03-02T12:11:28.023Z  INFO 30923 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-02T12:11:28.051Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : Started TemperatureApplication in 3.372 seconds (process running for 4.028)

</pre>

Generate a request from another terminal using curl (or from a browser or postman)

<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab/section04/activity]$ curl "localhost:8080/simulateTemperature?measurements=5&location=Paris"

[21,28,29,35,27]
</pre>


## Check the results in the Datadog UI (APM traces)

Visually you would be able to notice that across the tests (before and after the span attributes were set) the service widget was replaced from custom to web. And that the resource name has been changed from `temperatureSimulation` to `GET /simulateTemperature` when setting the attribute **manually**


<p align="left">
  <img src="img/springotel41.png" width="850" />
</p>


This example shows the attribute set using semantic conventions

<p align="left">
  <img src="img/springotel42.png" width="850" />
</p>



To view the generated traces: https://app.datadoghq.com/apm/traces

## End



