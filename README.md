
# OpenTelemetry java lab - the definitive working example showing various techniques to trace a Java application using Otel


## Introduction

The purpose of this tutorial is to cover various activities around tracing using OpenTelemetry. Each activity is covered in a dedicated section.
The structure is as follows:


* section01: Overview of the java application that will be used troughout this tutorial (Spring Boot application that exposes a single endpoint)
* section02: Automatic instrumentation using of the Otel java agent
* section03: Manual instrumentation
* section04: Practical example of manual tracing where spans are generated using the Otel java sdk (ex of creating a simple span, then multiple spans with parent/childhood relationships)
* section05: Otel SDK
* section06: Manual tracing creating a parent span and a child span explicitly (By passing a Tracer instance) 
* section07: Manual tracing creating a parent span and a child span implicitly (By relying on automatic assignment of the parent)
* section08: Manual tracing and how to use attributes to change the span type, service names etc...
* section09: Manual tracing to use Baggage item
* section10: Manual tracing covering inter-process communication using the tracer.inject()/extract() methods for context propagation
* section11: Otel API
* section12: Manual tracing using the Otel API and the Otel java agent (use of the tracer loaded by the Otel java agent)
* section13: Manual tracing using the Otel API and the DD java agent (use of the tracer loaded by the DD java agent)
* section14: Sending other Otel telemetry signals (logs and metrics)
* section15: Manual tracing using annotations
* section16: Using peer.service
* section17: Configuring the AutoConfigureSDK introducing the env variables and system properties.

Misc
* section18: Using Jaeger alongside DD
* section19: Using the Otel gateway deployment
* section20: Configuring http to send traces to the Otel collector instead of Otlp
* section21: Asynchronous activities examples and tracing across thread boundaries (Using an example based on Scala and Akka)
* section22: Docker and Kubernetes integration



In each section, we'll describe the required steps to take in order to reach the goal.
The activities in this lab follow a logical order so that we can get to the more advanced concepts smoothly.

The content of each topic can be found in the corresponding directory `section0X`. Each directory will contain two sub directories (named `activity` and `solution`). The `activity` directory will contain the initial state of the project and the provided instructions will guide you through the details that need to be covered. If you don't wish to follow them or simply need to view the code examples corresponding to the final state of the activity you will find them in the `solution` directory


## Goal of this activity (`main` branch)

This exercise is only meant to familiarize yourself with the structure of the project (directory structure, file names) but also the steps to follow to build, run and test the application.
There won't be much change in the code. Therefore, no solution branch created.



## Pre-requisites

+ About 15/30 minutes for each activity
+ A java JDK (If building & running locally). Ex OpenJDK 17 or above
+ Gradle installed (If building & running locally). Ex Gradle 7.5.1
+ Git client
+ A Datadog account with a valid API key
+ Your favorite text editor or IDE (Ex Sublime Text, Atom, vscode...)
+ Docker and docker-compose.



## Clone the repository


<pre style="font-size: 12px">
[root@pt-instance-1:~/]$ git clone https://github.com/ptabasso2/oteljavalab
[root@pt-instance-1:~/]$ cd oteljavalab
[root@pt-instance-1:~/oteljavalab]$ 
</pre>



## Directory structure of the project

The example below is the structure after having built the app.

<pre style="font-size: 12px">

[root@pt-instance-1:~/]$ tree
.
├── README.md
├── build
│   ├── classes
│   │   └── java
...
│   ├── generated
...
│
│   ├── libs
│   │   └── springtest4-1.0.jar
│   ├── resources
│   │   └── main
│   │       └── application.properties
│   └── tmp
│       ├── bootJar
│       │   └── MANIFEST.MF
│       └── compileJava
│           └── source-classes-mapping.txt
├── build.gradle
├── commands
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── logs
│   └── sprinttest4.log
└── src
    └── main
        ├── java
        │   └── com
        │       └── datadoghq
        │           └── pej
        │               ├── Application.java
        │               └── BasicController.java
        └── resources
            └── application.properties

31 directories, 16 files

</pre>

The main components of this project can be described as follows:
+ The `src` directory that contains the two class forming our app. The Application class will contain the implementation details to bootstrap the app. It can be seen as the class exposing the main method that will spin up the app. </br>
  The `BasicController` class will contain the details related to the endpoint that will be exposed. </br> There is also a configuration files that will contain the properties / settings that will be used by the application. </br> In the current version, it contains the logger configuration settings
+ The `build.gradle` file is the build configuraton file used by gradle.
+ The `build` directory contains the generated classes and archive file resulting from the compilation/build steps.


## Build the application

<pre style="font-size: 12px">
COMP10619:~ pejman.tabassomi$ ./gradlew build

Deprecated Gradle features were used in this build, making it incompatible with Gradle 7.0.
Use '--warning-mode all' to show the individual deprecation warnings.
See https://docs.gradle.org/6.9.1/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 1s
3 actionable tasks: 3 executed

</pre>


At this stage, the artifact that will be produced (`springtest4-1.0.jar`) will be placed under the `./build/libs` directory that gets created during the build process.


## Run the application

Running the application is fairly simple:

<pre style="font-size: 12px">
COMP10619:~ pejman.tabassomi$ java -jar build/libs/springtest4-1.0.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.2.2.RELEASE)

2022-02-06 19:27:12 [main] INFO  com.datadoghq.pej.Application - Starting Application on COMP10619.local with PID 30132 (/Users/pejman.tabassomi/SpringTest4/build/libs/springtest4-1.0.jar started by pejman.tabassomi in /Users/pejman.tabassomi/SpringTest4)
2022-02-06 19:27:12 [main] INFO  com.datadoghq.pej.Application - No active profile set, falling back to default profiles: default
2022-02-06 19:27:13 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port(s): 8080 (http)
2022-02-06 19:27:13 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
2022-02-06 19:27:13 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/9.0.29]
2022-02-06 19:27:13 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
2022-02-06 19:27:13 [main] INFO  o.s.web.context.ContextLoader - Root WebApplicationContext: initialization completed in 914 ms
2022-02-06 19:27:13 [main] INFO  o.s.s.c.ThreadPoolTaskExecutor - Initializing ExecutorService 'applicationTaskExecutor'
2022-02-06 19:27:13 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) with context path ''
2022-02-06 19:27:13 [main] INFO  com.datadoghq.pej.Application - Started Application in 6.833 seconds (JVM running for 7.26)

</pre>

The application will start a Tomcat server that will load our application that will be listening to connection on port 8080.


## Test the application

In another terminal run the following command, you should receive the answer `Ok`

<pre style="font-size: 12px">
COMP10619:~ pejman.tabassomi$ curl localhost:8080/Callme
Ok
</pre>

## Check the results in the Datadog UI (APM traces)
https://app.datadoghq.com/apm/traces
