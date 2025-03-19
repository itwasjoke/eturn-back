package com.eturn.eturn.security.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EtuIdUser {

    @JsonProperty("etu_id")
    private String etuId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("second_name")
    private String secondName;

    @JsonProperty("educations")
    private List<EtuIdEducation> educations;

    @JsonProperty("worker_positions")
    private  List<EtuIdWorkerPosition> etuIdWorkerPositions;

}

