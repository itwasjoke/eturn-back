package com.eturn.eturn.entity;

import com.eturn.eturn.enums.EduEnum;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
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
