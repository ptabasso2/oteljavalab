
# Overview of the java application that will be used in this tutorial.


## Goal of this activity

This exercise is only meant to familiarize yourself with the structure of the project (directory structure, file names) but also the steps to follow to build, run and test the application.
There won't be much change in the code yet. Therefore the `solution` won't contain anything.


## Overview

The Temperature Simulation Application is a Spring Boot-based service designed to simulate temperature measurements for given locations. This application provides an API endpoint that allows users to request simulated temperature readings over a specified number of measurements, offering a flexible tool for generating temperature data for various use cases, such as testing, data analysis, or educational purposes.

## Features

- **Simulate temperature readings:** Generate a series of temperature measurements within a specified range, simulating real-world temperature variations.
- **Customizable measurements:** Users can specify the number of temperature readings they wish to generate, allowing for flexibility in data simulation.
- **Optional location specification:** While primarily focused on temperature generation, the application allows for an optional location parameter to contextualize the simulated data.
- **Logging and monitoring:** Logs temperature simulation details, including the specified location (if provided) and the generated temperature readings, facilitating debugging and monitoring.

## How it works

The application consists of three main components:

1. **TemperatureApplication:** The entry point to the Spring Boot application, responsible for launching the service.
2. **TemperatureController:** A REST controller that handles HTTP GET requests to the `/simulateTemperature` endpoint. This controller accepts parameters for the location (optional) and the number of temperature measurements to simulate.
3. **Thermometer:** A model class that simulates the temperature readings. It generates a list of integer values representing the temperatures, based on the specified number of measurements and a predefined temperature range.

## Usage

To use the application, send a GET request to the `/simulateTemperature` endpoint with the desired number of measurements and, optionally, a location. The application will return a list of simulated temperature readings for the specified conditions.

Example request:
```
curl "localhost:8080/simulateTemperature?measurements=5&location=Paris"
```

This request would simulate five temperature measurements for Paris, returning a list of integers representing the temperatures.

## Installation and running

1. Clone the repository to your local machine.
2. Build the project using Maven or Gradle (depending on your preference).
3. Run the application.
4. Access the API through your preferred HTTP client or browser to start simulating temperatures.


In the following we will assume that docker is being used as per the descriptions in the previous chapter. But if you wish to build, run and test locally this will work just fine as the project has the same structure inside or outside the docker container.

## Directory structure of the project


If you haven't done so already, you will want to bootsrap first the containers
(Make sure the `DD_API_KEY` and `DD_SITE` env variables are set)   


```bash
[root@pt-instance-1:~/oteljavalab]$ DD_SITE="your_site_value" DD_API_KEY="your_api_key_value" docker-compose up -d
Creating otel-collector ... done
Creating springotel     ... done
```

Let's connect to the application container (`springotel`):

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab]$ docker exec -it springotel bash
[root@pt-instance-1:~/oteljavalab]$ 
</pre>


And then let's view the content of the `activity` directory in `section01`

<pre style="font-size: 12px">

[root@pt-instance-1:~/oteljavalab]$ cd section01/activity
[root@pt-instance-1:~/oteljavalab/section01/activity]$ tree
.
├── build.gradle.kts
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
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

11 directories, 9 files

</pre>


## Build the application


If you're not using docker, first make sure that your environment is correctly set up by checking the gradle and jdk versions 

Gradle
<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ gradle -version

------------------------------------------------------------
Gradle 8.4
------------------------------------------------------------

Build time:   2023-10-04 20:52:13 UTC
Revision:     e9251e572c9bd1d01e503a0dfdf43aedaeecdc3f

Kotlin:       1.9.10
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          17.0.9 (Eclipse Adoptium 17.0.9+9)
OS:           Linux 6.5.0-1008-gcp amd64
</pre>

JDK
<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ java -version 
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)
OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9, mixed mode, sharing)
</pre>


Now we will simply run the command to build gradle task to build the project. This will generate an additional `build` directory that will contain the artifact that will be used to run our service.

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ gradle build

BUILD SUCCESSFUL in 22s
4 actionable tasks: 4 executed

</pre>


At this stage, the artifact that will be produced (`springotel-0.0.1-SNAPSHOT.jar`) will be placed under the `./build/libs` directory that gets created during the build process.


## Run the application

Running the application is fairly simple:

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ java -jar build/libs/springotel-0.0.1-SNAPSHOT.jar
2024-03-01T20:53:54.849Z  INFO 3899506 --- [           main] c.p.o.s.TemperatureApplication           : Starting TemperatureApplication v0.0.1-SNAPSHOT using Java 17.0.9 with PID 3899506 (/root/oteljavalab/section01/activity/build/libs/springotel-0.0.1-SNAPSHOT.jar started by root in /root/oteljavalab/section01/activity)
2024-03-01T20:53:54.854Z  INFO 3899506 --- [           main] c.p.o.s.TemperatureApplication           : No active profile set, falling back to 1 default profile: "default"
2024-03-01T20:53:56.380Z  INFO 3899506 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2024-03-01T20:53:56.395Z  INFO 3899506 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2024-03-01T20:53:56.396Z  INFO 3899506 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.18]
2024-03-01T20:53:56.446Z  INFO 3899506 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2024-03-01T20:53:56.449Z  INFO 3899506 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1447 ms
2024-03-01T20:53:56.934Z  INFO 3899506 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-03-01T20:53:56.954Z  INFO 3899506 --- [           main] c.p.o.s.TemperatureApplication           : Started TemperatureApplication in 2.711 seconds (process running for 3.338)

</pre>

The application will start a Tomcat server that will load our application that will be listening to connections on port 8080.


## Test the application

In another terminal run the following command **from within the container**, you should receive something like `[29,34,35,21,24]`

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ curl "localhost:8080/simulateTemperature?measurements=5&location=Paris"
[27, 33, 34, 22, 20]
</pre>


If you check the output of the first terminal, you should see those new log entries generated

<pre style="font-size: 12px">
...
2024-03-01T21:51:53.349Z  INFO 3917734 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2024-03-01T21:51:53.350Z  INFO 3917734 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2024-03-01T21:51:53.351Z  INFO 3917734 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
2024-03-01T21:51:53.403Z  INFO 3917734 --- [nio-8080-exec-1] c.p.o.s.TemperatureController            : Temperature simulation for Paris: [27, 33, 34, 22, 20]
...
</pre>

## Tearing down the services

Exit the container

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ exit
[root@pt-instance-1:~/oteljavalab/section01/activity]$ 
</pre>

Graceful shutdown

<pre style="font-size: 12px">
[root@pt-instance-1:~/oteljavalab/section01/activity]$ docker-compose down
Stopping otel-collector ... done
Stopping springotel     ... done
Removing otel-collector ... done
Removing springotel     ... done
</pre>


## Conclusion

The Temperature simulation application offers a flexible tool for generating simulated temperature data. Its easy-to-use API and customizable parameters make it suitable for a wide range of applications, from development and testing environments to educational settings.



## End

