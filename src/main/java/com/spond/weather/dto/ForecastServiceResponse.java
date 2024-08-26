package com.spond.weather.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastServiceResponse {

    private boolean success;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorResponseDTO error;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ForecastDTO forecastData;

}
