package com.pej.otel.springotellab;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
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

    // TextMapGetter to extract context from HttpServletRequest
    private static final TextMapGetter<HttpServletRequest> getter = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            return (Iterable<String>) carrier.getHeaderNames();
        }

        @Override
        public String get(HttpServletRequest carrier, String key) {
            return carrier.getHeader(key);
        }
    };


    @Autowired
    public CalculatorController(Thermometer thermometer, OpenTelemetry openTelemetry) {
        this.thermometer = thermometer;
        this.tracer = openTelemetry.getTracer(CalculatorController.class.getName(), "0.1.0");

    }

    @GetMapping("/measureTemperature")
    public int measure(HttpServletRequest request) {
        // Extract the context
        Context context = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), request, getter);

        // Start a new span as a child of the extracted context
        Span span = tracer.spanBuilder("measure")
                .setParent(context)
                .startSpan();


        try (Scope scope = span.makeCurrent()) {
            // Your business logic here
            return thermometer.measureOnce();
        } finally {
            span.end(); // Make sure to end the span
        }
    }

}
