package io.temp.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class CalculatorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CalculatorApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }
}
