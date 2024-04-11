package com.pej.otel.springotellab;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class Thermometer {

    private int minTemp;
    private int maxTemp;

    private final Tracer tracer;

    private final Meter meter;

    private LongCounter temperatureMeasurementsCounter;

    @Autowired
    Thermometer(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(Thermometer.class.getName(), "0.1.0");
        this.meter = openTelemetry.getMeter("TemperatureMeter");
        this.temperatureMeasurementsCounter = meter.counterBuilder("temperature_measurements")
                .setDescription("Counts the number of temperature measurements made")
                .setUnit("1")
                .build();
    }

    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();
        Span parentSpan = tracer.spanBuilder("simulateTemperature").startSpan();
        try (Scope scope = parentSpan.makeCurrent()) {
            for (int i = 0; i < measurements; i++) {
                temperatures.add(this.measureOnce());
                temperatureMeasurementsCounter.add(1);
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

    public void setTemp(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }
}