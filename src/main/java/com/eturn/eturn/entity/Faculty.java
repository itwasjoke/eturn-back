package com.eturn.eturn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "faculty")
public class Faculty {
    @Id
    private Long id;
    private String name;
    @ManyToMany(mappedBy = "allowedFaculties",fetch = FetchType.LAZY)
    private Set<Turn> turns;
    @OneToMany(mappedBy = "faculty", fetch = FetchType.LAZY)
    private Set<Group> groups;
}
