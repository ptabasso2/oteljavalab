package com.pej.otel.springotellab;

import java.util.List;
import java.util.Optional;
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

    @Autowired
    Thermometer thermometer;


    @GetMapping("/simulateTemperature")
    public List<Integer> index(@RequestParam("location") Optional<String> location,
                               @RequestParam("measurements") Optional<Integer> measurements) {

        if (measurements.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing measurements parameter", null);
        }

        List<Integer> result = thermometer.simulateTemperature(measurements.get());

        if (location.isPresent()) {
            logger.info("Temperature simulation for {}: {}", location.get(), result);
        } else {
            logger.info("Temperature simulation for an unspecified location: {}", result);
        }
        return result;
    }
}
