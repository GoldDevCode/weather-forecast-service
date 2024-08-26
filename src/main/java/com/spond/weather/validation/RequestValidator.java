package com.spond.weather.validation;

import com.spond.weather.dto.ErrorResponseDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.spond.weather.constants.ApplicationConstants.*;
import static java.time.ZoneOffset.UTC;

public class RequestValidator {

    public static Optional<ErrorResponseDTO> validateRequest(double latitude,
                                                             double longitude,
                                                             String startDateTimeStamp,
                                                             String endDateTimeStamp) {

        Optional<ErrorResponseDTO> dateValidation = validateDates(startDateTimeStamp, endDateTimeStamp);

        if (dateValidation.isPresent()) {
            return dateValidation;
        }

        return validateLocationCoordinates(latitude, longitude);

    }

    private static Optional<ErrorResponseDTO> validateDates(String startDateTimeStamp, String endDateTimeStamp) {
        // Check for null or empty strings
        if (startDateTimeStamp == null || startDateTimeStamp.isEmpty()) {
            return Optional.of(ErrorResponseDTO.builder()
                    .errorCode(ERROR_CODE_VALIDATION_ERROR)
                    .errorDescription(ERROR_DESCRIPTION_INVALID_DATE)
                    .build());
        }
        if (endDateTimeStamp == null || endDateTimeStamp.isEmpty()) {
            return Optional.of(ErrorResponseDTO.builder()
                    .errorCode(ERROR_CODE_VALIDATION_ERROR)
                    .errorDescription(ERROR_DESCRIPTION_INVALID_DATE)
                    .build());
        }

        try {
            Instant startTimestamp = Instant.parse(startDateTimeStamp);
            Instant endTimestamp = Instant.parse(endDateTimeStamp);
            Instant now = Instant.now().atZone(UTC).toInstant();

            // Check if start timestamp is less than end timestamp
            if (startTimestamp.isAfter(endTimestamp)) {
                return Optional.of(ErrorResponseDTO.builder()
                        .errorCode(ERROR_CODE_VALIDATION_ERROR)
                        .errorDescription(ERROR_DESCRIPTION_START_DATE_BEFORE_END_DATE)
                        .build());
            }

            // Check if end timestamp is not in the past
            if (startTimestamp.isBefore(now) || endTimestamp.isBefore(now)) {
                return Optional.of(ErrorResponseDTO.builder()
                        .errorCode(ERROR_CODE_VALIDATION_ERROR)
                        .errorDescription(ERROR_DESCRIPTION_DATE_IN_PAST)
                        .build());
            }

            // Check if the timestamps are within 7 days from the current date
            Instant sevenDaysLater = now.plus(7, ChronoUnit.DAYS);

            if (startTimestamp.isAfter(sevenDaysLater) || endTimestamp.isAfter(sevenDaysLater)) {
                return Optional.of(ErrorResponseDTO.builder()
                        .errorCode(ERROR_CODE_VALIDATION_ERROR)
                        .errorDescription(ERROR_DESCRIPTION_EVENT_NOT_WITHIN_7_DAYS)
                        .build());
            }

        } catch (Exception e) {
            return Optional.of(ErrorResponseDTO.builder()
                    .errorCode(ERROR_CODE_VALIDATION_ERROR)
                    .errorDescription(ERROR_DESCRIPTION_INVALID_DATE_FORMAT)
                    .build());
        }
        return Optional.empty();
    }

    private static Optional<ErrorResponseDTO> validateLocationCoordinates(double latitude,
                                                                          double longitude) {
        if (latitude < -90 || latitude > 90) {
            return Optional.of(ErrorResponseDTO.builder()
                    .errorCode(ERROR_CODE_VALIDATION_ERROR)
                    .errorDescription(ERROR_DESCRIPTION_INVALID_LATITUDE)
                    .build());
        }
        if (longitude < -180 || longitude > 180) {
            return Optional.of(ErrorResponseDTO.builder()
                    .errorCode(ERROR_CODE_VALIDATION_ERROR)
                    .errorDescription(ERROR_DESCRIPTION_INVALID_LONGITUDE)
                    .build());
        }

        //validate that latitude and longitude are actually numbers
        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            return Optional.of(ErrorResponseDTO.builder()
                    .errorCode(ERROR_CODE_VALIDATION_ERROR)
                    .errorDescription(ERROR_DESCRIPTION_INVALID_LOCATION_FORMAT)
                    .build());
        }

        return Optional.empty();
    }

}
