package com.pej.otel.springotellab;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class TemperatureApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TemperatureApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().build();
    }


    @Bean
    public OpenTelemetry openTelemetry() {

        Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "springsimul").build();

        OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder().setTimeout(2, TimeUnit.SECONDS).build();

        SdkTracerProvider setTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcSpanExporter).setScheduleDelay(100, TimeUnit.MILLISECONDS).build())
                .setResource(resource)
                .build();

        return OpenTelemetrySdk.builder().setTracerProvider(setTracerProvider).setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).buildAndRegisterGlobal();

    }
}
