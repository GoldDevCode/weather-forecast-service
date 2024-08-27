package com.spond.weather;

import com.spond.weather.dto.ErrorResponseDTO;
import com.spond.weather.validation.RequestValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RequestValidatorTest {

    private RequestValidator requestValidator;

    private static String utcTimestampStart;
    private static String utcTimestampEnd;

    @BeforeAll
    static void setUp() {
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.plusHours(2);

        // Convert LocalDateTime to ZonedDateTime in UTC
        ZonedDateTime zonedDateTimeStart = startDateTime.atZone(ZoneOffset.UTC);
        ZonedDateTime zonedDateTimeEnd = endDateTime.atZone(ZoneOffset.UTC);

        // Format the ZonedDateTime to a UTC timestamp string
        utcTimestampStart = zonedDateTimeStart.format(DateTimeFormatter.ISO_INSTANT);
        utcTimestampEnd = zonedDateTimeEnd.format(DateTimeFormatter.ISO_INSTANT);
    }

    @BeforeEach
    void setUpEach() {
        MockitoAnnotations.openMocks(this);
        requestValidator = new RequestValidator();
    }

    @Test
    void testValidRequest() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, utcTimestampStart, utcTimestampEnd);
        assertFalse(result.isPresent());
    }

    @Test
    void testInvalidLatitude() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(100.00, 9.58, utcTimestampStart, utcTimestampEnd);
        assertTrue(result.isPresent());
        assertEquals("Invalid latitude", result.get().getErrorDescription());
    }

    @Test
    void testInvalidLongitude() {


        System.out.println(utcTimestampStart);
        System.out.println(utcTimestampEnd);

        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 200.00, utcTimestampStart, utcTimestampEnd);
        assertTrue(result.isPresent());
        assertEquals("Invalid longitude", result.get().getErrorDescription());
    }

    @Test
    void testInvalidStartTime() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, "invalid-time", "2023-10-01T16:00:00Z");
        assertTrue(result.isPresent());
        assertEquals("Invalid date format", result.get().getErrorDescription());
    }

    @Test
    void testInvalidEndTime() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, "2023-10-01T14:00:00Z", "invalid-time");
        assertTrue(result.isPresent());
        assertEquals("Invalid date format", result.get().getErrorDescription());
    }

    @Test
    void testEndTimeBeforeStartTime() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, "2024-08-27T16:00:00Z", "2024-08-27T14:00:00Z");
        assertTrue(result.isPresent());
        assertEquals("Start date should be before end date", result.get().getErrorDescription());
    }

    @Test
    void testStartTimeInThePast() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, "2024-08-25T14:00:00Z", "2024-08-27T14:00:00Z");
        assertTrue(result.isPresent());
        assertEquals("Start/End date cannot be in the past", result.get().getErrorDescription());
    }

    @Test
    void testEventNotWithin7Days() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, "2024-09-25T14:00:00Z", "2024-09-25T16:00:00Z");
        assertTrue(result.isPresent());
        assertEquals("Event start date should be within 7 days", result.get().getErrorDescription());
    }

    @Test
    void testInvalidDateFormat() {
        Optional<ErrorResponseDTO> result = RequestValidator.validateRequest(60.10, 9.58, "2023-10-01", "2023-10-01T16:00:00Z");
        assertTrue(result.isPresent());
        assertEquals("Invalid date format", result.get().getErrorDescription());
    }
}