package com.pej.otel.springotellab;


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class TemperatureApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TemperatureApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public Tracer tracer(){
        return GlobalOpenTelemetry.getTracer(TemperatureApplication.class.getName(), "0.1.0");
    }

}
