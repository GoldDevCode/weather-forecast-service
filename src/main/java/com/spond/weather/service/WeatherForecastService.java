package com.spond.weather.service;

import com.spond.weather.dto.ForecastDTO;
import com.spond.weather.dto.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.spond.weather.util.ApplicationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherForecastService {

    private final Logger logger = LoggerFactory.getLogger(WeatherForecastService.class);

    private final RestTemplate restTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    private final static String WEATHER_API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%s&lon=%s";

    public Optional<ForecastDTO> getWeatherForecast(UUID eventId, double latitude, double longitude, String startTimeStamp, String endTimeStamp) {

        String cacheKey = "EventID:" + eventId;

        ForecastDTO cachedForecast = (ForecastDTO) redisTemplate.opsForValue().get(cacheKey);

        if (cachedForecast != null) {
            return Optional.of(cachedForecast);
        }

        try {

            String url = String.format(WEATHER_API_URL, latitude, longitude);

            ResponseEntity<WeatherApiResponse> response = restTemplate.getForEntity(url, WeatherApiResponse.class);

            WeatherApiResponse weatherApiResponse = response.getBody();

            ForecastDTO finalForeCast = prepareFinalForecast(weatherApiResponse, startTimeStamp, endTimeStamp);

            redisTemplate.opsForValue().set(cacheKey, finalForeCast, 2, TimeUnit.HOURS);

            return Optional.of(finalForeCast);

        } catch (Exception e) {
            logger.error("Exception occurred while getting weather forecast data", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private ForecastDTO prepareFinalForecast(WeatherApiResponse weatherApiResponse, String startTimeStamp, String endTimeStamp) {

        WeatherApiResponse filteredData = filterWeatherData(weatherApiResponse, startTimeStamp, endTimeStamp);

        List<ForecastDTO> forecastDTOList = mapResponseToDTO(filteredData);

        return calculateAverageForeCastForEvent(forecastDTOList);
    }

}
