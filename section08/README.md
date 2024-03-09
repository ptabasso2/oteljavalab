# Manual tracing: Otel API and the Datadog java agent


## Goal of this activity

This section will demonstrate how you can achieve the same objectives as the previous section, but this time by using the Datadog java agent. The main difference lies in the goals we aim to achieve. The benefit of using the Datadog agent is the ability to take advantage of features not yet available in the OpenTelemetry ecosystem, such as **continuous profiling**, **application security**, and **dynamic instrumentation**. 

These can be leveraged while still adding custom instrumentation using OpenTelemetry. The instructions regarding code modification remain unchanged; therefore, we won't repeat them here. Instead, we will focus on the specifics of enabling these capabilities with the Datadog agent. 


## Main steps

* Setting up the Datadog Agent
* Downloading the Datadog java agent.
* Custom instrumentation using the Otel API (cf previous section)
* Building, running and testing the app  


## Architecture overview

## Setting up the Datadog Agent

A specific docker-compose file (`docker-compose-section08.yml`) is provided and contains the necessary settings to spin up the Datadog agent container configured to receive traces. And another service to start the application container containing our Spring application.  

### Setting up the environment

1. First let's stop the previous containers by running the following command on the docker host. 

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ docker-compose down
Stopping springotel     ... done
Stopping otel-collector ... done
Removing springotel     ... done
Removing otel-collector ... done
</pre>

2. Set the environment variable **DD_API_KEY** with the value of your API key

3. Loading the new configuration by using the corresponding docker-compose file `docker-compose-section08.yml`

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ docker-compose -f docker-compose-section08.yml up -d
Creating dd-agent-dogfood-jmx ... done
Creating springotel           ... done
</pre>


4. Accessing the application container

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ docker exec -it springotel bash
[root@pt-instance-1:/oteljavalab]$ 
</pre>


2. Navigating to the project directory.

<pre style="font-size: 12px">
[root@pt-instance-1:/oteljavalab]$ cd section08/activity
[root@pt-instance-1:/oteljavalab/section08/activity]$
</pre>


3. Download the Datadog java agent

<pre style="font-size: 12px">
[root@pt-instance-1:/oteljavalab/section08/activity]$ wget -O dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
</pre>


## Instrumentation without the Otel API using the Datadog java agent.

For this we will simply run our application by passing the `-javaagent` pointing to the Datadog java agent as a JVM option.

<pre style="font-size: 12px">

[root@pt-instance-1:/oteljavalab/section08/solution]$ java -javaagent:dd-java-agent.jar -Ddd.service=springotel -jar build/libs/springtotel-0.0.1-SNAPSHOT.jar 
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[dd.trace 2024-03-09 22:47:07:699 +0000] [dd-task-scheduler] INFO datadog.trace.agent.core.StatusLogger - DATADOG TRACER CONFIGURATION {"version":"1.31.1~37358b9aa1","os_name":"Linux","os_version":"6.5.0-1008-gcp","architecture":"amd64","lang":"jvm","lang_version":"17.0.9","jvm_vendor":"Eclipse Adoptium","jvm_version":"17.0.9+9","java_class_version":"61.0","http_nonProxyHosts":"null","http_proxyHost":"null","enabled":true,"service":"springotel","agent_url":"http://localhost:8126","agent_error":false,"debug":false,"trace_propagation_style_extract":["datadog","tracecontext"],"trace_propagation_style_inject":["datadog","tracecontext"],"analytics_enabled":false,"sampling_rules":[{},{}],"priority_sampling_enabled":true,"logs_correlation_enabled":true,"profiling_enabled":false,"remote_config_enabled":true,"debugger_enabled":false,"appsec_enabled":"ENABLED_INACTIVE","telemetry_enabled":true,"telemetry_dependency_collection_enabled":true,"telemetry_log_collection_enabled":false,"dd_version":"","health_checks_enabled":true,"configuration_file":"no config file present","runtime_id":"43d1f66d-f837-4e43-8e4d-bd7b095dbd96","logging_settings":{"levelInBrackets":false,"dateTimeFormat":"'[dd.trace 'yyyy-MM-dd HH:mm:ss:SSS Z']'","logFile":"System.err","configurationFile":"simplelogger.properties","showShortLogName":false,"showDateTime":true,"showLogName":true,"showThreadName":true,"defaultLogLevel":"INFO","warnLevelString":"WARN","embedException":false},"cws_enabled":false,"cws_tls_refresh":5000,"datadog_profiler_enabled":true,"datadog_profiler_safe":true,"datadog_profiler_enabled_overridden":false,"data_streams_enabled":false}
2024-03-09T22:47:09.069Z  INFO 424 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 424 (/oteljavalab/section08/solution/build/libs/springtotel-0.0.1-SNAPSHOT.jar started by root in /oteljavalab/section08/solution)
2024-03-09T22:47:09.100Z  INFO 424 --- [           main] c.p.o.s.TemperatureApplication           : No active profile set, falling back to 1 default profile: "default"
2024-03-09T22:47:11.116Z  INFO 424 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-03-09T22:47:11.187Z  INFO 424 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-03-09T22:47:11.187Z  INFO 424 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.18]
2024-03-09T22:47:11.254Z  INFO 424 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-03-09T22:47:11.256Z  INFO 424 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 2025 ms
2024-03-09T22:47:11.846Z  INFO 424 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-09T22:47:11.860Z  INFO 424 --- [           main] c.p.o.s.TemperatureApplication           : Started TemperatureApplication in 3.883 seconds (process running for 6.252)
</pre>


And then we can send a request and observe the result

<p align="left">
  <img src="img/springotel81.png" width="850" />
</p>


What we get is fairly comparable to the result obtained when using the OpenTelemetry java agent except that in the case of the Datadog agent we get two spans (One for the `servlet.request` operation and the second for the controller handler method `index()`). 

But like the Otel java agent, the Datadog java agent doesn't capture either the details associated to the `Thermometer` methods `simulateTemperature()` and `measureOnce()`.   

To get around this we will proceed as we did previously by adding the Otel API and instrument manually those methods.

## Manual instrumentation using the Otel API

Same instructions as the previous section that describes how to:

* Use the API and adding the necessary dependency to the project
* Gain access to the `Tracer` instance
* Create any additional span to the ones the java agent is creating


## Build, run and test the application

After having modified and rebuilt the application, let's run and test it again.
 
<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab/section08/solution]$ java -javaagent:dd-java-agent.jar -Ddd.service=springotel -Ddd.trace.otel.enabled=true -jar build/libs/springtotel-0.0.1-SNAPSHOT.jar 
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[dd.trace 2024-03-09 23:03:09:968 +0000] [dd-task-scheduler] INFO datadog.trace.agent.core.StatusLogger - DATADOG 
...
...
...
2024-03-09T23:03:13.656Z  INFO 3598988 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-09T23:03:13.670Z  INFO 3598988 --- [           main] c.p.o.s.TemperatureApplication           : Started TemperatureApplication in 3.381 seconds (process running for 5.753)

</pre>


### Observations about the command executed

- `java`: This invokes the JVM to start the java application.

- `-javaagent:dd-java-agent.jar`: This option specifies the Datadog java agent (`dd-java-agent.jar`) that should be attached to the JVM. The Datadog java agent provides automatic instrumentation for a wide range of java frameworks and libraries, enabling the collection of traces and metrics without modifying the application code.

- `-Ddd.service=springotel`: This system property (`-D`) sets the name of the service as `springotel`. In Datadog, service names are used to group related traces, logs and metrics, making it easier to navigate and monitor the application's performance.

- `-Ddd.trace.otel.enabled=true`: This option enables the OpenTelemetry interoperability within the Datadog java agent. By setting this property to `true`, we are allowing the Datadog agent to consume telemetry data (traces, metrics) using the OpenTelemetry protocol. This is particularly useful if transitioning from OpenTelemetry to Datadog or if using tools and libraries that are instrumented with OpenTelemetry.

- `-jar build/libs/springtotel-0.0.1-SNAPSHOT.jar`: This part of the command specifies that the JVM should run the application packaged in the JAR file `build/libs/springtotel-0.0.1-SNAPSHOT.jar`. This is the application's JAR, tied to our Spring Boot application.


### Test the application and check the results in the Datadog UI

Generate a request from another terminal using curl (or from a browser or postman) either locally on the host or from the container 

<pre style="font-size: 12px">

[root@pt-instance-1:/oteljavalab/section07/activity]$ curl "localhost:8080/simulateTemperature?measurements=5&location=Paris"

[21,28,29,35,27]
</pre>


This will produce the following trace

<p align="left">
  <img src="img/springotel82.png" width="850" />
</p>

This demonstrates that the previously missing spans are now visible and correctly correlated with those generated by the automatic instrumentation.

To view the generated traces: https://app.datadoghq.com/apm/traces

## End

