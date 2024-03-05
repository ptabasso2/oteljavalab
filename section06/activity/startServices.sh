#!/bin/bash


echo "Starting temperature-calculator service..."
nohup java -jar temperature-calculator/build/libs/springtempcalc-0.0.1-SNAPSHOT.jar --server.port=8088 > temperature-calculator.log 2>&1 &

echo "Starting temperature-simulator service..."
nohup java -jar temperature-simulator/build/libs/springtempsimu-0.0.1-SNAPSHOT.jar > temperature-simulator.log 2>&1 &

echo "Services are starting in the background. Logs are available in temperature-calculator.log and temperature-simulator.log"

