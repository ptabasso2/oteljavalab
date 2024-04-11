package io.temp.simulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;

@Component
public class Thermometer {
    private static final Logger logger = LoggerFactory.getLogger(Thermometer.class);

    private int minTemp;
    private int maxTemp;

    @Autowired
    private RestTemplate restTemplate;

    private String url = System.getenv("DATA_PROCESSING_ADDR");

    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();

        for (int i = 0; i < measurements; i++) {
            ResponseEntity<String> response = restTemplate.getForEntity(url + "/processTemperature", String.class);
            temperatures.add(Integer.valueOf(response.getBody()));
        }
        return temperatures;
    }
}
