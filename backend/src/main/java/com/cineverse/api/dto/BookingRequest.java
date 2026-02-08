package com.cineverse.api.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private Long screeningId;
    private String seat;
}
