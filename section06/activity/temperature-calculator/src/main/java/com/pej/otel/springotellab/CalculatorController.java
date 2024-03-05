package com.pej.otel.springotellab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalculatorController {
    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);

    private final Thermometer thermometer;

    @Autowired
    public CalculatorController(Thermometer thermometer){
        this.thermometer = thermometer;
    }

    @GetMapping("/measureTemperature")
    public int measure() {
        return thermometer.measureOnce();
    }

}
