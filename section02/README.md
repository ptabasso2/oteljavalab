
# Automatic instrumentation using of the Otel java agent


## Goal of this activity

In the previous sections we went through the set up the Otel collector and an overview of the java application will be using.
In this chapter we will see how to use the opentelemetry java agent to perform automatic instrumentation of our service


## Steps

1. Build the application
2. Download the java agent
3. Add `-javaagent:path/to/opentelemetry-javaagent.jar` and other config to your JVM startup arguments and launch your app


## Environment set-up

Download `opentelemetry-javaagent.jar` from Releases of the opentelemetry-java-instrumentation repository. The JAR file contains the agent and instrumentation libraries.

<pre style="font-size: 12px">
[root@pt-instance-1:~/]$ cd oteljavalab/section02/activity
[root@pt-instance-1:~/oteljavalab/section02/activity]$ wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
</pre>


## Directory structure of the project

The example below is the structure after having built the app.

<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab/section02/activity]$ tree
.
├── build.gradle.kts
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── opentelemetry-javaagent.jar
├── settings.gradle.kts
└── src
    └── main
        ├── java
        │   └── com
        │       └── pej
        │           └── otel
        │               └── springotellab
        │                   ├── TemperatureApplication.java
        │                   ├── TemperatureController.java
        │                   └── Thermometer.java
        └── resources
            └── application.properties

11 directories, 10 files

</pre>


## Build the application

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section02/activity]$ gradle assemble
Starting a Gradle Daemon (subsequent builds will be faster)

BUILD SUCCESSFUL in 12s
4 actionable tasks: 4 executed
</pre>


## Run the application

Running the application is fairly simple:

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section02/activity]$ java -javaagent:./opentelemetry-javaagent.jar -Dotel.service.name=springotel -Dotel.logs.exporter=none -jar build/libs/springotellab-0.0.1-SNAPSHOT.jar
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[otel.javaagent 2024-03-01 23:48:35:776 +0000] [main] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: 2.1.0
2024-03-01T23:48:39.817Z  INFO 3960341 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 3960341 (/root/oteljavalab/section02/activity/build/libs/springotellab-0.0.1-SNAPSHOT.jar started by root in /root/oteljavalab/section02/activity)
2024-03-01T23:48:39.865Z  INFO 3960341 --- [           main] c.p.o.s.TemperatureApplication           : No active profile set, falling back to 1 default profile: "default"
2024-03-01T23:48:41.419Z  INFO 3960341 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-03-01T23:48:41.452Z  INFO 3960341 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-03-01T23:48:41.453Z  INFO 3960341 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.18]
2024-03-01T23:48:41.525Z  INFO 3960341 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-03-01T23:48:41.528Z  INFO 3960341 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1524 ms
2024-03-01T23:48:42.087Z  INFO 3960341 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-01T23:48:42.102Z  INFO 3960341 --- [           main] c.p.o.s.TemperatureApplication           : Started TemperatureApplication in 3.092 seconds (process running for 6.552)
</pre>

The service will start and will run our application that listens to connections on port 8080.

Note: cf https://opentelemetry.io/docs/languages/java/automatic/#configuring-the-agent for more details about how to configure the java agent

## Test the application

In another terminal run the following command, you should see the following output `[23, 33, 35, 33, 35]`

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section02/activity]$ curl "localhost:8080/simulateTemperature?measurements=5&location=Paris"
[23, 33, 35, 33, 35]
</pre>

## Check the results in the Datadog UI (APM traces)
https://app.datadoghq.com/apm/traces


<p align="left">
  <img src="img/springotel0.png" width="850" />
</p>


