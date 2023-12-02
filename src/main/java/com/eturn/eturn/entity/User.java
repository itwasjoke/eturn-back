package com.eturn.eturn.entity;

import com.eturn.eturn.enums.RoleEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long idGroup;

    private Long idCourse;

    private Long idDepartment;

    private Long idFaculty;

    @Enumerated(EnumType.STRING)
    private RoleEnum roleEnum;

    @OneToMany(mappedBy = "user")
    private List<Turn> turns;

    @OneToMany(mappedBy = "user")
    private List<Position> positions;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }

    public void setIdCourse(Long idCourse) {
        this.idCourse = idCourse;
    }

    public void setIdDepartment(Long idDepartment) {
        this.idDepartment = idDepartment;
    }

    public void setIdFaculty(Long idFaculty) {
        this.idFaculty = idFaculty;
    }

    public void setRoleEnum(RoleEnum roleEnum) {
        this.roleEnum = roleEnum;
    }

    public void setTurnList(List<Turn> turns) {
        this.turns = turns;
    }
}
