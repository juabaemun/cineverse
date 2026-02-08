package com.cineverse.api.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class StarWarsMovieDTO {
    @JsonProperty("title")
    private String title;

    @JsonProperty("opening_crawl")
    private String openingCrawl; // Usaremos esto como sinopsis

    @JsonProperty("episode_id")
    private Integer episodeId;
}