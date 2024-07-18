package com.eturn.eturn.dto.parsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartmentResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("longTitle")
    private String longTitle;

    @JsonProperty("type")
    private String type;

    @JsonProperty("groups")
    private List<GroupResponse> groupResponses;
}
