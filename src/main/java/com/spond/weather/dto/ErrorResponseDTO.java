package com.spond.weather.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorResponseDTO {

    private String errorCode;
    private String errorDescription;

}
