package com.eturn.eturn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    private Long facultyId;

    @ManyToMany(mappedBy = "allowedGroups")
    private Set<Turn> turns;
}
