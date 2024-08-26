package com.spond.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spond.weather.dto.ForecastDTO;
import com.spond.weather.dto.WeatherApiResponse;
import com.spond.weather.service.WeatherForecastService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Testcontainers
class WeatherForecastServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private WeatherForecastService weatherService;

    private UUID eventId;
    private double latitude;
    private double longitude;
    private String start;
    private String end;

    @BeforeEach
    void setUp() {

        String address = redisContainer.getHost();
        Integer port = redisContainer.getMappedPort(6379);
        System.setProperty("spring.data.redis.host", address);
        System.setProperty("spring.data.redis.port", port.toString());
        //MockitoAnnotations.openMocks(this);
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testWeatherForecastCachingHit() throws IOException {

        eventId = UUID.randomUUID();
        latitude = 60.10;
        longitude = 9.58;
        start = "2024-08-26T14:00:00Z";
        end = "2024-08-26T16:00:00Z";
        String cacheKey = "EventID:" + eventId;

        ForecastDTO forecastDTO = new ForecastDTO();
        forecastDTO.setAirTemperature(10.0);
        forecastDTO.setWindSpeed(5.0);

        redisTemplate.opsForValue().set(cacheKey, forecastDTO);

        Optional<ForecastDTO> result = weatherService.getWeatherForecast(eventId, latitude, longitude, start, end);

        assertTrue(result.isPresent());
        assertEquals(10.0, result.get().getAirTemperature());
        assertEquals(5.0, result.get().getWindSpeed());

        // Verify that RestTemplate was not called
        verify(restTemplate, Mockito.never())
                .getForObject(any(String.class), eq(WeatherApiResponse.class));

    }

    @Test
    void testWeatherForecastCachingMiss() throws IOException {

        eventId = UUID.randomUUID();
        latitude = 60.10;
        longitude = 9.58;
        start = "2024-08-26T14:00:00Z";
        end = "2024-08-26T16:00:00Z";
        String cacheKey = "EventID:" + eventId;

        // Create a WeatherApiResponse object from file met_response.json in src/test/resources
        WeatherApiResponse mockResponse = objectMapper.readValue(getClass().getClassLoader().getResourceAsStream("met_response.json"), WeatherApiResponse.class);

        ResponseEntity<WeatherApiResponse> responseEntity = ResponseEntity.ok(mockResponse);

        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), eq(WeatherApiResponse.class)))
                .thenReturn(responseEntity);

        Optional<ForecastDTO> result = weatherService.getWeatherForecast(eventId, latitude, longitude, start, end);

        assertTrue(result.isPresent());
        assertEquals(14.7, result.get().getAirTemperature());
        assertEquals(4.1, result.get().getWindSpeed());

        // Verify that RestTemplate was called once
        verify(restTemplate, times(1)).getForEntity(Mockito.anyString(), eq(WeatherApiResponse.class));

        // Verify that the data is now in cache
        ForecastDTO cachedForecast = (ForecastDTO) redisTemplate.opsForValue().get(cacheKey);
        assertNotNull(cachedForecast);
        assertEquals(14.7, cachedForecast.getAirTemperature());
        assertEquals(4.1, cachedForecast.getWindSpeed());

        Optional<ForecastDTO> result2 = weatherService.getWeatherForecast(eventId, latitude, longitude, start, end);

        assertTrue(result2.isPresent());
        assertEquals(14.7, result2.get().getAirTemperature());
        assertEquals(4.1, result2.get().getWindSpeed());

        verify(restTemplate, Mockito.never())
                .getForObject(any(String.class), eq(WeatherApiResponse.class));
    }
}
