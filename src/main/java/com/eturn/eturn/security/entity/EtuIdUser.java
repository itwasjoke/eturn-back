package com.eturn.eturn.security.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Игнорируем неизвестные поля
public class EtuIdUser {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("second_name")
    private String secondName;

    @JsonProperty("position")
    private String position;

    @JsonProperty("educations")
    private List<EtuIdEducation> educations; // Замените Object на нужный тип, если известен

}

