package com.eturn.eturn.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EduGroups {
    @JsonProperty("name")
    private String name;
    @JsonProperty("faculty")
    private FacultyResponse facultyResponse;
}
