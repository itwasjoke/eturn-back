package com.eturn.eturn.entity;

import com.eturn.eturn.enums.EduEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EduEnum eduEnum;

    private Integer number;

    @ManyToMany(mappedBy = "allowedCourses",fetch = FetchType.LAZY)
    private Set<Turn> turns;

}
