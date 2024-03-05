# Manual tracing: How to propagate context across services.


## Goal of this activity

In this section of the OpenTelemetry tutorial, we delve into the crucial aspect of context propagation across service boundaries, specifically focusing on interactions via REST calls using the OpenTelemetry Java SDK. Context propagation is fundamental in distributed tracing as it ensures that trace context is maintained when moving from one service to another. This continuity allows us to trace requests through various microservices in a system, providing a comprehensive view of a transaction or workflow.

We will cover the following key areas:

1. **Introduction to Context Propagation**: Begin with an overview of what context propagation is and why it's essential for distributed tracing in microservices architectures. This includes explaining the concepts of traces and spans, and how context is used to link spans across service boundaries.

2. **OpenTelemetry Context and Propagators**: Introduce the OpenTelemetry constructs for context and propagation. Explain how the `Context` object carries data, including trace information, across process boundaries, and how `Propagators` are used to inject and extract context from carrier formats (such as HTTP headers).

3. **Configuring the Propagation Mechanisms**: Detail the steps to configure the default or custom context propagation mechanisms in the OpenTelemetry Java SDK. This will involve setting up the `TextMapPropagator` for HTTP headers, which is commonly used for REST calls.

4. **Instrumenting a REST Call**: Provide a step-by-step guide on instrumenting a REST call from one service to another. This includes how to:
   - Use the OpenTelemetry API to create a new span for the outgoing request.
   - Inject the current context into the HTTP headers of the outgoing request.
   - Extract the context from the incoming request headers in the called service.
   - Create a new span in the called service as a child of the extracted span context.

5. **Example Implementation**: Offer a concise example implementation of two microservices interacting over HTTP, where service A calls service B. Include code snippets showing how to instrument the REST client in service A and the server in service B using OpenTelemetry Java SDK.

6. **Best Practices for Context Propagation**: Conclude with a discussion on best practices to ensure robust and efficient context propagation. Highlight the importance of consistent propagation techniques across services and the potential pitfalls to avoid.

By the end of this section, readers will have a solid understanding of how to implement context propagation using the OpenTelemetry Java SDK for services communicating over REST calls. This knowledge will enable them to trace requests seamlessly across service boundaries, improving observability and aiding in debugging and monitoring distributed systems.


## Overview of the set-up

We are going to transition from a single Spring Boot application that performs temperature measurements to two distinct services, namely a `Temperature Simulator` and a `Temperature Calculator`. Below is an explanation of the changes made and the rationale behind them:


### Original Architecture: Monolithic Application

Initially, the application was a monolith, where both the simulation of temperature measurements and the calculation of these measurements were handled within a single Spring Boot application. This setup included a `Thermometer` class responsible for generating simulated temperature values and possibly performing calculations directly within the same service.

### Transition to Microservices: Temperature Simulator and Temperature Calculator

The transformation involves splitting the original monolithic application into two microservices:


<p align="left">
  <img src="img/springotel61.png" width="720" />
</p>



1. **Temperature Simulator**: This service is responsible for simulating temperature measurements. It acts as the entry point for requests, simulating the generation of temperature data. However, instead of calculating the temperature values directly, it delegates the responsibility of performing calculations to the Temperature Calculator service.

    - **Key Changes**:
        - The Temperature Simulator retains the functionality to initiate temperature measurement simulations but now includes the capability to make HTTP requests to the Temperature Calculator for any required calculations.
        - It uses `RestTemplate` to call the Temperature Calculator service, passing necessary data through HTTP requests.
        - Tracing is incorporated to ensure that requests between the Temperature Simulator and the Temperature Calculator are part of the same trace for observability.

2. **Temperature Calculator**: This new microservice is solely responsible for performing calculations related to temperature. It exposes endpoints that the Temperature Simulator can call to retrieve calculated temperature values.

    - **Key Changes**:
        - A new Spring Boot application is created to serve as the Temperature Calculator.
        - It defines endpoints (e.g., `/measureTemperature`) that accept requests for temperature calculations, processes them, and returns the results.
        - OpenTelemetry tracing is integrated to extract the tracing context from incoming requests, ensuring that the distributed tracing spans across both the simulator and calculator services.

### Integration and Observability

- **Context Propagation**: To maintain observability across microservices, the OpenTelemetry framework is used to propagate tracing context between the Temperature Simulator and the Temperature Calculator. This ensures that calls between services are linked within the same trace.
- **Service Communication**: The Temperature Simulator service uses HTTP clients (`RestTemplate` or `WebClient`) to communicate with the Temperature Calculator, passing necessary information for calculations.

### Benefits of This Approach

- **Scalability**: Separating concerns into distinct services allows each microservice to scale independently based on demand.
- **Maintainability and Flexibility**: Changes can be made to the calculation logic or simulation algorithm independently, reducing the risk of impacting the other functionality.
- **Focused Responsibility**: Each service has a clear, focused responsibility, enhancing the cohesion of the service.

### Conclusion

This transformation from a monolithic application to a microservices architecture enables better scalability, flexibility, and maintainability of the services. By leveraging Spring Boot for each microservice and integrating OpenTelemetry for distributed tracing, the solution provides a robust framework for simulating and calculating temperatures while ensuring comprehensive observability across service boundaries.






























* Using the sdk and adding the necessary dependencies to the project 
* Gaining access to a `Tracer` instance
* Create a simple trace
* Add metadata and tag to our trace

We will use the following basic features of the OpenTelemetry API:


* a `Tracer` instance is used to create a span using a span builder via the `spanBuilder()` method.
* Each `span` is given a **name**
* The span gets started via the `startSpan()` method.
* each `span` must be finished by calling its `end()` method and this happens inside a scope.
* For a basic setup, you might choose the OTLP protocol to send the various observability signals (Traces, logs, metrics). It is a versatile prootocol and supported by many backends.


## Adding the sdk to the project

In order to do so, we will simply add the following dependencies to the dependency bloc of the `build.gradle.kts` file

This should look like

```java
dependencies {
        compile("org.springframework.boot:spring-boot-starter-web")
        implementation("io.opentelemetry:opentelemetry-api")
	    implementation("io.opentelemetry:opentelemetry-sdk")
	    implementation("io.opentelemetry:opentelemetry-exporter-logging")
	    implementation("io.opentelemetry.semconv:opentelemetry-semconv:1.23.1-alpha")
	    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.35.0")

}
```

## Instantiate a tracer

In order to get an instance of our tracer, we leverage Spring's "dependency injection" capability through which the Spring container “injects” objects into other objects or “dependencies”. This tracer object is accessed through an object of type `OpenTelemetry` that needs to be created first.

For this we will declare a Bean inside the Application class `TemperatureApplication`. This mainly consists of annotating the following method using the `@Bean` annotation. This bean can later be accessed from the other classes by relying on Spring's dependeny injection mechanisms (this happens by using the `@Autowired` annotation). This annotation allows Spring to resolve and inject collaborating beans into other beans.

We will actually refer to it later in the `TemperatureController` class. 

Simply put we will first declare a bean in the `Application` class to instantiate an OpenTelemetry object


Let's first add the following block in the `TemperatureApplication` class *after* the `main()` method:

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

**Note**: At this point, you will also need to consider importing the various classes manually that are needed if you use a Text editor or they will be inferred if you use an IDE (IntelliJ or VSCode).
If you have to do it manually, add the following to the import section of your `TemperatureApplication` class

```java

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

```

Now in `TemperatureController` we will need to get a hold on the `OpenTelemetry` object so that we can create a tracer instance. For this we need to add the following lines immediately after the Logger instance declaration:

```java

    private final Tracer tracer; // (1)


    @Autowired   // (2)
    TemperatureController(OpenTelemetry openTelemetry) {
       tracer = openTelemetry.getTracer(TemperatureController.class.getName(), "0.1.0");
    }
```

(1) declaring the tracer variable and (2) using constructor injection to initialize the tracer. The OpenTelemetry object provides a getTracer() method that allows this. 


The corresponding packages to import are:

```java
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
```


## Creating a span

It's time now to build and start spans in the `TemperatureController` class. And we can replicate the same steps in anay other classes that contains methods we need to instrument.


Now that we can access the `Tracer` instance, let's add the tracing idioms in our code:
We will change the method implementation as follows:

Example with the `index()` method:

**_Before_**

```java
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
```

**_After_**

```java
        Span span = tracer.spanBuilder("temperatureSimulation").startSpan();
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

**Note**: At this point, you will also need to consider importing the various classes manually that are needed if you use a Text editor.
This is generally handled _automatically_ by IDEs (IntelliJ or Eclipse).
If you have to do it manually, add the following to the import section of your `TemperatureController` class

```java
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
```


### Observations

#### Before: Without Instrumentation
- **Functionality**: This code block performs a temperature simulation operation based on the number of measurements requested. If the `measurements` parameter is missing, it throws an exception. It logs the result of the simulation, which varies depending on whether the `location` is specified.
- **Observability**: The observability in this snippet is limited to logging. It logs the outcome of the temperature simulation but doesn't provide deeper insights into the operation's execution, such as performance metrics, errors, or the operation's context in a larger transaction.

#### After: With Manual Instrumentation
- **Instrumentation Introduction**: This snippet introduces manual instrumentation by wrapping the temperature simulation logic within a span. A span represents a single unit of work within a larger trace, allowing for detailed monitoring and analysis of the operation.
- **Span Creation**: At the beginning of the operation, a new span named `temperatureSimulation` is started. This explicitly marks the start of an operation that you want to monitor.
- **Scope Management**: The operation is enclosed within a try-with-resources statement that ensures the span's scope is correctly managed. The `scope` ensures that the `span` is considered the current active span within its block, which is crucial for correct tracing in asynchronous or multi-threaded environments.
- **Error Handling**: The catch block captures any thrown exceptions, allowing the span to record these exceptions. This is valuable for debugging and monitoring, as it directly associates errors with the operation that caused them.
- **Span Closure**: Finally, the span is ended in the finally block, marking the completion of the operation. Ending a span is crucial for accurate measurement of operation duration and for ensuring resources are correctly freed.
- **Enhanced Observability**: With the span in place, the operation now contributes to a trace, providing insights into performance, errors, and the operation's relationship to other work units. This enhanced observability is invaluable for troubleshooting, performance tuning, and understanding system behavior.

#### Summary of Differences
The key difference lies in the enhanced observability provided by manual instrumentation. While the first snippet relies solely on logging for observability, the second snippet uses OpenTelemetry spans to offer detailed insights into the operation's execution, including performance metrics and error tracking. This manual instrumentation allows developers and operators to better understand, monitor, and debug their applications, especially in complex, distributed systems.


## Creating additonal spans (child span)

Our plan is now to custom instrument the methods that are inside the `Thermometer` class (`simulateTemperature()` and `measureOnce()`). Here is how the change might look like:

#### Before: Without Instrumentation

```java

public List<Integer> simulateTemperature(int measurements) {
    List<Integer> temperatures = new ArrayList<Integer>();
    for (int i = 0; i < measurements; i++) {
        temperatures.add(this.measureOnce());
    }
    return temperatures;
}


private int measureOnce() {
    return ThreadLocalRandom.current().nextInt(this.minTemp, this.maxTemp + 1);
}


public void setTemp(int minTemp, int maxTemp){
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
}

```


The initial version of the `Thermometer` class is straightforward: it simulates temperature measurements without any observability into its operations beyond what could be logged or inferred externally. This simplicity is fine for basic operations but lacks the depth needed for troubleshooting, performance monitoring, and understanding the behavior in complex or distributed systems.

- The `simulateTemperature` method generates a list of random temperatures, simulating measurements.
- The `measureOnce` method generates a single temperature measurement.
- The `setTemp` method that acts as a setter method to set the temperature bounds

This design is functional but opaque; without external logs, there's no insight into how many measurements were taken, how long they took, or whether any issues occurred during the process.



#### After: With Manual Instrumentation

```java
private final Tracer tracer;

@Autowired
Thermometer(OpenTelemetry openTelemetry) {
   tracer = openTelemetry.getTracer(Thermometer.class.getName(), "0.1.0");
}

public List<Integer> simulateTemperature(int measurements) {
    List<Integer> temperatures = new ArrayList<Integer>();
    Span parentSpan = tracer.spanBuilder("simulateTemperature").startSpan();
    try (Scope scope = parentSpan.makeCurrent()){
        for (int i = 0; i < measurements; i++) {
            temperatures.add(this.measureOnce());
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
```


The instrumented version introduces OpenTelemetry spans to provide visibility into the execution of temperature simulations. This change allows developers and operators to trace the execution of temperature measurements, offering insights into the system's behavior, performance characteristics, and potential issues.

- The constructor is extended to accept a `Tracer` object, enabling the creation of spans within the class methods.
- The `simulateTemperature` method now starts a span before generating temperature measurements, making this operation observable as a discrete unit of work in traces. The span is made the current active span, ensuring that any spans created within this context (such as those in `measureOnce`) are correctly nested as children.
- The `measureOnce` method also starts a span for each individual temperature measurement. This granular level of instrumentation provides insight into the performance and behavior of the temperature generation process itself, which could be critical for diagnosing issues or optimizing the simulation.

### Key Benefits and Differences

- **Visibility and Debuggability**: The addition of spans makes the temperature simulation process transparent and observable. It's now possible to trace each operation, see how long it takes, and monitor for errors or anomalies.
- **Context Propagation**: By making spans the current context, the changes ensure that the trace context is propagated correctly through the operations. This means that `measureOnce` operations are correctly recognized as part of the larger `simulateTemperature` operation, allowing for accurate representation of operation hierarchy in traces.
- **Performance Monitoring**: With spans, you can now monitor the performance of both the overall temperature simulation and individual measurements. This can help identify bottlenecks or inefficiencies in the simulation logic.
- **Error Detection**: Span error recording allows for immediate visibility into exceptions or issues within the simulated operations, facilitating quicker diagnosis and resolution.

In summary, the instrumentation of the `Thermometer` class with OpenTelemetry spans transforms it from a black box into a transparent, observable component of your application. This enhances the ability to monitor, debug, and optimize your application, providing crucial insights into its behavior and performance.

## Build, run and test the application

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section03/activity]$ gradle build

BUILD SUCCESSFUL in 4s
4 actionable tasks: 4 executed

[root@pt-instance-1:~/oteljavalab/section03/activity]$ java -jar build/libs/springotellab-0.0.1-SNAPSHOT.jar &
2024-03-02T12:11:25.450Z  INFO 30923 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 30923 (/root/oteljavalab/section03/activity/build/libs/springotellab-0.0.1-SNAPSHOT.jar started by root in /root/oteljavalab/section03/activity)
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

[root@pt-instance-1:~/oteljavalab/section03/activity]$ curl "localhost:8080/simulateTemperature?measurements=5&location=Paris"

[21,28,29,35,27]
</pre>


## Check the results in the Datadog UI (APM traces)

<p align="left">
  <img src="img/springotel3.png" width="850" />
</p>


To view the generated traces: https://app.datadoghq.com/apm/traces

## Final remark

At this stage, the objective is well achieved, we managed to instrument our application 
using the instrumentation api and the spans and traces are sent to the backend after 
having been processed by the Datadog Agent.
But we have not detailed the points related to the dependency of the spans between them. 


