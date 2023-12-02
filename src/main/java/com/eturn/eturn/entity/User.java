package com.eturn.eturn.entity;

import com.eturn.eturn.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name="user")
public class User {
    @Id
    private Long id;

    private String name;

    private Long idGroup;

    private Long idCourse;

    private Long idDepartment;

    private Long idFaculty;

    @Enumerated(EnumType.ORDINAL)
    private RoleEnum roleEnum;

    @OneToMany(targetEntity = Turn.class, cascade = CascadeType.ALL)
    private List<Turn> turnList;

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

    public void setTurnList(List<Turn> turnList) {
        this.turnList = turnList;
    }
}
