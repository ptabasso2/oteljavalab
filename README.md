
![GitHub License](https://img.shields.io/github/license/ptabasso2/oteljavalab)
![GitHub Release](https://img.shields.io/github/v/release/ptabasso2/oteljavalab)
![GitHub last commit](https://img.shields.io/github/last-commit/ptabasso2/oteljavalab)


# OpenTelemetry java lab - Various techniques to trace a Java application


## Introduction

The purpose of this tutorial is to cover various activities around tracing using OpenTelemetry. Each activity is covered in a dedicated section.
The structure is as follows:


* section01: Overview of the java application that will be used troughout this tutorial (Spring Boot application that exposes a single endpoint)
* section02: Automatic instrumentation using of the Otel java agent
* section03: Manual tracing: Span creation and management using the Otel SDK
* section04: Manual tracing and how to use attributes to change the span type, service names etc...
* section05: Manual tracing to use Baggage item
* section06: Manual tracing covering inter-process communication using the tracer.inject()/extract() methods for context propagation
* section07: Manual tracing using the Otel API and the Otel java agent (use of the tracer loaded by the Otel java agent)
* section08: Manual tracing using the Otel API and the DD java agent (use of the tracer loaded by the DD java agent)



In each section, we'll describe the required steps to take in order to reach the goal.
The activities in this lab follow a logical order so that we can get to the more advanced concepts smoothly.

The content of each topic can be found in the corresponding directory `section0X`. Each directory will contain two sub directories (named `activity` and `solution`). The `activity` directory will contain the initial state of the project and the provided instructions will guide you through the details that need to be covered. If you don't wish to follow them or simply need to view the code examples corresponding to the final state of the activity you will find them in the `solution` directory


## Pre-requisites

+ About 15/30 minutes for each activity
+ A java JDK (If building & running locally). Ex OpenJDK 17 or above
+ Gradle installed (If building & running locally). Ex Gradle 7.5.1
+ Git client
+ A Datadog account with a valid API key
+ Your favorite text editor or IDE (Ex Sublime Text, Atom, vscode...)


## Clone the repository


<pre style="font-size: 12px">
[root@pt-instance-1:~/]$ git clone https://github.com/ptabasso2/oteljavalab
[root@pt-instance-1:~/]$ cd oteljavalab
[root@pt-instance-1:~/oteljavalab]$ 
</pre>

