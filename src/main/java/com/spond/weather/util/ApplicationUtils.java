package com.spond.weather.util;

import com.spond.weather.dto.ForecastDTO;
import com.spond.weather.dto.WeatherApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ApplicationUtils {

    static Logger logger = LoggerFactory.getLogger(ApplicationUtils.class);

    public static WeatherApiResponse filterWeatherData(WeatherApiResponse weatherApiResponse, String startTimeStamp, String endTimeStamp) {
        // Filter the weather data based on the start and end timestamps of the event
        // and return the filtered data
        try {


            LocalDateTime startTime = LocalDateTime.ofInstant(Instant.parse(startTimeStamp), ZoneId.ofOffset("UTC", ZoneOffset.UTC));
            LocalDateTime endTime = LocalDateTime.ofInstant(Instant.parse(endTimeStamp), ZoneId.ofOffset("UTC", ZoneOffset.UTC));

            // Calculate the maximum date allowed (7 days from now)
            LocalDateTime maxAllowedDate = LocalDateTime.now().plusDays(7);

            List<WeatherApiResponse.TimeSeries> filteredData = new java.util.ArrayList<>(weatherApiResponse.getProperties().getTimeseries().stream()
                    .filter(data -> {
                        LocalDateTime forecastTime = LocalDateTime.ofInstant(Instant.parse(data.getTime()), ZoneId.ofOffset("UTC", ZoneOffset.UTC));
                        // Filter conditions:
                        // 1. Forecast time is within the provided start and end time or
                        // 2. Forecast time is within the next 7 days from now

                        return (forecastTime.isAfter(startTime) || forecastTime.isEqual(startTime)) &&
                                (forecastTime.isBefore(endTime) || forecastTime.isEqual(endTime)) &&
                                forecastTime.isBefore(maxAllowedDate);
                    })
                    .toList());

            //Condition if no data is found between/or matching the start and end time
            if (filteredData.isEmpty()) {

                List<WeatherApiResponse.TimeSeries> closestStartTimeForeCast = findClosestForecast(weatherApiResponse.getProperties().getTimeseries(), startTime, maxAllowedDate);
                List<WeatherApiResponse.TimeSeries> closestEndTimeForeCast = findClosestForecast(weatherApiResponse.getProperties().getTimeseries(), endTime, maxAllowedDate);

                filteredData.addAll(closestStartTimeForeCast);
                filteredData.addAll(closestEndTimeForeCast);
            }

            logger.info("Filtered data size: {}", filteredData);

            weatherApiResponse.getProperties().setTimeseries(filteredData);

            return weatherApiResponse;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static List<ForecastDTO> mapResponseToDTO(WeatherApiResponse weatherApiResponse) {

        return weatherApiResponse.getProperties()
                .getTimeseries()
                .stream()
                .map(timeSeries -> {
                    ForecastDTO forecastDTO = new ForecastDTO();
                    forecastDTO.setAirTemperature(timeSeries.getData()
                            .getInstant()
                            .getDetails()
                            .getAir_temperature());
                    forecastDTO.setWindSpeed(timeSeries.getData()
                            .getInstant()
                            .getDetails()
                            .getWind_speed());
                    return forecastDTO;
                })
                .toList();

    }

    public static ForecastDTO calculateAverageForeCastForEvent(List<ForecastDTO> forecastDTOList) {
        // Calculate the average air temperature and wind speed round off to 1 decimal place
        double avgAirTemperature = forecastDTOList.stream()
                .mapToDouble(ForecastDTO::getAirTemperature)
                .average()
                .orElse(0.0);

        double avgWindSpeed = forecastDTOList.stream()
                .mapToDouble(ForecastDTO::getWindSpeed)
                .average()
                .orElse(0.0);

        ForecastDTO finalForeCast = new ForecastDTO();
        finalForeCast.setAirTemperature(Math.round(avgAirTemperature * 10.0) / 10.0);
        finalForeCast.setWindSpeed(Math.round(avgWindSpeed * 10.0) / 10.0);

        return finalForeCast;
    }

    private static List<WeatherApiResponse.TimeSeries> findClosestForecast(List<WeatherApiResponse.TimeSeries> timeSeriesList, LocalDateTime eventTime, LocalDateTime maxAllowedDate) {
        return timeSeriesList.stream()
                .filter(
                        data -> {
                            LocalDateTime forecastTime = LocalDateTime.ofInstant(Instant.parse(data.getTime()), ZoneId.ofOffset("UTC", ZoneOffset.UTC));
                            return forecastTime.isBefore(maxAllowedDate);
                        }
                )
                .sorted(Comparator.comparing(data -> {
                    LocalDateTime forecastTime = LocalDateTime.ofInstant(Instant.parse(data.getTime()), ZoneId.ofOffset("UTC", ZoneOffset.UTC));
                    return Math.abs(forecastTime.toEpochSecond(ZoneOffset.UTC) - eventTime.toEpochSecond(ZoneOffset.UTC));
                }))
                .limit(1)
                .toList();
    }
}
