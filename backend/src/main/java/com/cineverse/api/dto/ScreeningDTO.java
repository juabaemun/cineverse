package com.cineverse.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScreeningDTO {
    private Long movieId;
    private Long roomId;
    private LocalDateTime startTime;
    private Double price;
}
