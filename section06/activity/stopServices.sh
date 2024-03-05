#!/bin/bash

# Ports used by the services
CALCULATOR_PORT=8088
SIMULATOR_PORT=8080

# Function to kill a process by port
kill_process_on_port() {
    PORT=$1
    PID=$(lsof -t -i:$PORT)
    if [ ! -z "$PID" ]; then
        echo "Killing process on port $PORT with PID $PID"
        kill -9 $PID
    else
        echo "No process found listening on port $PORT"
    fi
}

# Kill the services
kill_process_on_port $CALCULATOR_PORT
kill_process_on_port $SIMULATOR_PORT

echo "Services have been stopped."
rm temperature-calculator.log temperature-simulator.log 
