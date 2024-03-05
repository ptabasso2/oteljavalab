package com.pej.otel.springotellab;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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


    private String url = "http://localhost:8088/measureTemperature";

    public Thermometer(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer(Thermometer.class.getName(), "0.1.0");
    }


    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();
        Span parentSpan = tracer.spanBuilder("simulateTemperature").startSpan();

        try (Scope scope = parentSpan.makeCurrent()) {
            for (int i = 0; i < measurements; i++) {
                HttpHeaders headers = new HttpHeaders();
                TextMapSetter<HttpHeaders> setter = HttpHeaders::set;
                //Map<String, String> headers = new HashMap<>();
                GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, setter);
                //W3CTraceContextPropagator.getInstance().inject(Context.current(), headers, (carrier, key, value) -> headers.put(key, value));
                //W3CTraceContextPropagator.getInstance().inject(Context.current(), headers, setter);
                //openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), headers, setter);

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Integer> response = restTemplate.exchange(url, HttpMethod.GET, entity, Integer.class);
                temperatures.add(response.getBody());
            }
            return temperatures;
        } finally {
            parentSpan.end();
        }
    }
}
