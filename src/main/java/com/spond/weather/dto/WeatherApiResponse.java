package com.spond.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {
    private Properties properties;

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private List<TimeSeries> timeseries;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeSeries {
        private String time;
        private Data data;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private InstantDetails instant;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InstantDetails {
        private TemperatureDetails details;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemperatureDetails {
        private double air_temperature;
        private double wind_speed;
    }
}
