package com.pej.otel.springotellab;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class Thermometer {

    private int minTemp;
    private int maxTemp;

    @Autowired
    private RestTemplate restTemplate;

    private final Tracer tracer;

    public Thermometer(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(Thermometer.class.getName(), "0.1.0");
    }

    private String url = "http://localhost:8088/measureTemperature";


    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();
        Span parentSpan = tracer.spanBuilder("simulateTemperature").startSpan();

        try (Scope scope = parentSpan.makeCurrent()) {
            for (int i = 0; i < measurements; i++) {
                ResponseEntity<Integer> response = restTemplate.getForEntity(url, Integer.class);
                temperatures.add(response.getBody());
            }
            return temperatures;
        } finally {
            parentSpan.end();
        }
    }
}
