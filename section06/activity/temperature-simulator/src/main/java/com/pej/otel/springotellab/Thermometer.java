package com.pej.otel.springotellab;

import org.springframework.beans.factory.annotation.Autowired;
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

    private String url = "http://localhost:8088/measureTemperature";


    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();
        for (int i = 0; i < measurements; i++) {
            ResponseEntity<Integer> response = restTemplate.getForEntity(url, Integer.class);
            temperatures.add(response.getBody());
        }
        return temperatures;
    }
}
