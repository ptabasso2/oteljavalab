package com.pej.otel.springotellab;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Thermometer {

    private int minTemp;
    private int maxTemp;

    private Tracer tracer;

    public Thermometer(int minTemp, int maxTemp, Tracer tracer) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.tracer = tracer;
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
}
