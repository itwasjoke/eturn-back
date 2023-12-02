package com.eturn.eturn.entity;

import com.eturn.eturn.enums.EduEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EduEnum eduEnum;

    private Integer number;

    public void setEduEnum(EduEnum eduEnum) {
        this.eduEnum = eduEnum;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
