package com.pej.otel.springotellab;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {
    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    private final Tracer tracer;


    private final Thermometer thermometer;

    @Autowired
    public CalculatorController(Thermometer thermometer, OpenTelemetry openTelemetry) {
        this.thermometer = thermometer;
        this.tracer = openTelemetry.getTracer(CalculatorController.class.getName(), "0.1.0");

    }

    @GetMapping("/measureTemperature")
    public int measure(HttpServletRequest request) {

        // Start a new span as a child of the extracted context
        Span span = tracer.spanBuilder("measure")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Your business logic here
            return thermometer.measureOnce();
        } finally {
            span.end(); // Make sure to end the span
        }
    }

}
