package io.temp.calculator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class Thermometer {

    @Value("${thermometer.minTemp}")
    private int minTemp;

    @Value("${thermometer.maxTemp}")
    private int maxTemp;

    public int measureOnce() {
        return ThreadLocalRandom.current().nextInt(this.minTemp, this.maxTemp + 1);
    }
}
