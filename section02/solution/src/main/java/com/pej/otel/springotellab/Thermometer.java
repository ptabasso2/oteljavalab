package com.pej.otel.springotellab;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class Thermometer {

    private int minTemp;
    private int maxTemp;

    public List<Integer> simulateTemperature(int measurements) {
        List<Integer> temperatures = new ArrayList<Integer>();
        for (int i = 0; i < measurements; i++) {
            temperatures.add(this.measureOnce());
        }
        return temperatures;
    }

    private int measureOnce() {
        return ThreadLocalRandom.current().nextInt(this.minTemp, this.maxTemp + 1);
    }

    public void setTemp(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }
}
