package com.eturn.eturn.dto.parsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class GroupResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("course")
    private int course;

    @JsonProperty("number")
    private String number;

    @JsonProperty("studyingType")
    private String studyingType;

    @JsonProperty("educationLevel")
    private String educationLevel;

    @JsonProperty("studyYears")
    private String studyYears;
}
