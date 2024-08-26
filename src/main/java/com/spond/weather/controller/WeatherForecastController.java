package com.spond.weather.controller;

import com.spond.weather.dto.ErrorResponseDTO;
import com.spond.weather.dto.ForecastServiceResponse;
import com.spond.weather.service.WeatherForecastService;
import com.spond.weather.validation.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.spond.weather.constants.ApplicationConstants.ERROR_CODE_INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
@Slf4j
public class WeatherForecastController {

    private final Logger logger = LoggerFactory.getLogger(WeatherForecastController.class);

    private final WeatherForecastService weatherForecastService;

    @GetMapping("/forecast/{eventId}")
    public ResponseEntity<?> getWeather(
            @PathVariable("eventId") UUID eventId,
            @RequestParam("latitude") double latitude,
                                        @RequestParam("longitude") double longitude,
                                        @RequestParam("startTimeStamp") String startTimeStamp,
                                        @RequestParam("endTimeStamp") String endTimeStamp) {
        logger.info("Request received for latitude: {} and longitude: {}", latitude, longitude);

        try {
            var isInvalidRequest = RequestValidator.validateRequest(latitude, longitude, startTimeStamp, endTimeStamp);

            if (isInvalidRequest.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ForecastServiceResponse.builder()
                                .success(false)
                                .error(isInvalidRequest.get())
                                .build());
            }

            var forecastData = weatherForecastService.getWeatherForecast(eventId, latitude, longitude, startTimeStamp, endTimeStamp);

            if (forecastData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ForecastServiceResponse.builder()
                                .success(false)
                                .error(ErrorResponseDTO.builder()
                                        .errorCode("NO_FORECAST_DATA")
                                        .errorDescription("No forecast data available for the given location and time range")
                                        .build())
                                .build());
            }


            return ResponseEntity.ok()
                    .body(ForecastServiceResponse.builder()
                            .success(true)
                            .forecastData(weatherForecastService.getWeatherForecast(eventId, latitude, longitude, startTimeStamp, endTimeStamp).get())
                            .build());
        } catch (Exception e) {
            logger.error("Error occurred while processing the request: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ForecastServiceResponse.builder()
                            .success(false)
                            .error(ErrorResponseDTO.builder()
                                    .errorCode(ERROR_CODE_INTERNAL_SERVER_ERROR)
                                    .errorDescription("Error occurred while processing the request")
                                    .build())
                            .build());
        }
    }

}
