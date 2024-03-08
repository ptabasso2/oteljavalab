package com.pej.otel.springotellab;

import java.util.List;
import java.util.Optional;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TemperatureController {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureController.class);
    private final Tracer tracer;

    @Autowired
    TemperatureController(Tracer tracer) {
        this.tracer = tracer;
    }

    @Autowired
    Thermometer thermometer;

    @GetMapping("/simulateTemperature")
    public List<Integer> index(@RequestParam("location") Optional<String> location,
                               @RequestParam("measurements") Optional<Integer> measurements) {

        Span span = tracer.spanBuilder("temperatureSimulation").startSpan();
	span.setAttribute("span.type", "web");
	span.setAttribute("resource.name", "GET /simulateTemperature");
        try (Scope scope = span.makeCurrent()) {

            if (measurements.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing measurements parameter", null);
            }

            thermometer.setTemp(20, 35);
            List<Integer> result = thermometer.simulateTemperature(measurements.get());

            if (location.isPresent()) {
                logger.info("Temperature simulation for {}: {}", location.get(), result);
            } else {
                logger.info("Temperature simulation for an unspecified location: {}", result);
            }
            return result;
        } catch(Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
