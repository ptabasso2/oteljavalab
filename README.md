
![GitHub License](https://img.shields.io/github/license/ptabasso2/oteljavalab)
![GitHub Release](https://img.shields.io/github/v/release/ptabasso2/oteljavalab)
![GitHub last commit](https://img.shields.io/github/last-commit/ptabasso2/oteljavalab)


# <img src="https://opentelemetry.io/img/logos/opentelemetry-logo-nav.png" alt="OTel logo" width="45"> OpenTelemetry java lab - Various techniques to trace a Java application


## Introduction

The purpose of this tutorial is to cover various activities around tracing using OpenTelemetry. Each activity is covered in a dedicated section.
The structure is as follows:

* **Section00**: Setting up the Otel collector contrib and the Datadog exporter
* **Section01**: Overview of the java application that will be used in this lab
* **Section02**: Automatic instrumentation using the Otel java agent
* **Section03**: Manual tracing: Span creation and management using the Otel SDK
* **Section04**: Manual tracing: How to set span attributes.
* **Section05**: Manual tracing: Automatic configuration of the SDK
* **Section06**: Manual tracing: How to propagate context across services.
* **Section07**: Manual tracing: Otel API and the Otel java agent
* **Section08**: Manual tracing: Otel API and the Datadog java agent.
* **Section09**: Sending other observability signals: Metrics
* **Section10**: Sending other observability signals: Logs (Log collection and connecting traces and logs)
* **Section11**: Manual tracing: Asynchronous activities and tracing across thread boundaries



In each section, we'll describe the required steps to take in order to reach the goal.
The activities in this lab follow a logical order so that we can get to the more advanced concepts smoothly.

The content of each topic can be found in the corresponding directory `section0X`. 

Each directory will contain two sub directories (named `activity` and `solution`). 

The `activity` directory will contain the initial state of the project and the provided instructions will guide you through the details that need to be covered. If you don't wish to follow them or simply need to view the code examples corresponding to the final state of the activity you will find them in the `solution` directory


## Pre-requisites

+ About 15/30 minutes for each activity
+ A java JDK (If building & running locally). Ex OpenJDK 17 or above
+ Gradle installed (If building & running locally). Ex Gradle 7.5.1
+ Git client
+ A Datadog account with a valid API key
+ Your favorite text editor or IDE (Ex Sublime Text, Atom, vscode...)
+ Docker (In which case Java and gradle won't be necessary)


## Clone the repository


<pre style="font-size: 12px">
[root@pt-instance-1:~/]$ git clone https://github.com/ptabasso2/oteljavalab
[root@pt-instance-1:~/]$ cd oteljavalab
[root@pt-instance-1:~/oteljavalab]$ 
</pre>


## Next steps

The next section of this tutorial covers the opentelemetry controller configuration and set up details. 