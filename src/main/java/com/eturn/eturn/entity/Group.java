package com.eturn.eturn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "groups", indexes = {
        @Index(name = "group_number", columnList = "number", unique = true)
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private Integer course;
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
    @ManyToMany(mappedBy = "allowedGroups",fetch = FetchType.LAZY)
    private Set<Turn> turns;
}
