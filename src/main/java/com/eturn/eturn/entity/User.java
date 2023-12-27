package com.eturn.eturn.entity;

import com.eturn.eturn.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String login;

    private String password;

    private String name;

    private Long idGroup;

    private Long idCourse;

    private Long idDepartment;

    private Long idFaculty;

    @Enumerated(EnumType.STRING)
    private RoleEnum roleEnum;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_turn",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "turn_id")
    )
    private Set<Turn> turns;

    @OneToMany(mappedBy = "creator")
    private Set<Turn> createdTurns;

    @OneToMany
    @JoinColumn(name = "turn_id")
    private Set<Position> positions;
}
