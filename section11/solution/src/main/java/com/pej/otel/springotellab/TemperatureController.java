package com.pej.otel.springotellab;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
public class TemperatureController {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureController.class);
    private final Tracer tracer;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    TemperatureController(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(TemperatureController.class.getName(), "0.1.0");
    }

    @Autowired
    Thermometer thermometer;

    @GetMapping("/simulateTemperature")
    public List<Integer> index(@RequestParam("location") Optional<String> location,
                               @RequestParam("measurements") Optional<Integer> measurements) throws Exception {

        Span span = tracer.spanBuilder("temperatureSimulation").startSpan();
        try (Scope scope = span.makeCurrent()) {

            if (measurements.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing measurements parameter", null);
            }

            ExecutorService wrappedExecutorService = Context.taskWrapping(executorService);

            Callable<List<Integer>> task = () -> {
                Span newSpan = tracer.spanBuilder("asyncTemperatureSimulation").startSpan();
                try (Scope newScope = newSpan.makeCurrent()) {
                    // Now 'newSpan' is the current span, and its context is active.
                    // Any spans created in this block will have 'newSpan' as their parent, which in turn has 'parentSpan' as its parent.
                    thermometer.setTemp(20, 35);
                    return thermometer.simulateTemperature(measurements.get());
                } finally {
                    newSpan.end(); // Ensure to end 'newSpan' after its work is done
                }
            };



            Future<List<Integer>> futureResult = wrappedExecutorService.submit(task);
            List<Integer> result = futureResult.get(); // This blocks until the task is completed and retrieves the result

            // Use the result as needed
            if (location.isPresent()) {
                logger.info("Temperature simulation for {}: {}", location.get(), result);
            } else {
                logger.info("Temperature simulation for an unspecified location: {}", result);
            }
            return result; // Return the result from the method
        } catch(Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
