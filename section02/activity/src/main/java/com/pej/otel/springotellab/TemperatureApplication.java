package com.pej.otel.springotellab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TemperatureApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TemperatureApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
