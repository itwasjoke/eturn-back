package com.eturn.eturn.entity;

import com.eturn.eturn.enums.TurnEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
// TODO @Setter
@Entity
@Table(name = "turn")
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(targetEntity = Position.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions;

    @OneToOne(targetEntity = User.class, cascade = CascadeType.ALL)
    private User creator;

    @Enumerated(EnumType.STRING)
    private TurnEnum turnEnum;

    @OneToMany(targetEntity = Group.class, cascade = CascadeType.ALL)
    private List<Group> allowedGroups;

    @OneToMany(targetEntity = Course.class, cascade = CascadeType.ALL)
    private List<Course> allowedCourses;

    @OneToMany(targetEntity = Faculty.class, cascade = CascadeType.ALL)
    private List<Faculty> allowedFaculties;

    @OneToMany(targetEntity = Department.class, cascade = CascadeType.ALL)
    private List<Department> allowedDepartments;

    //  @ManyToOne
    // private List<User> users;

    private Integer positionsCount;
    private Integer allTime;
    private Integer averageTime;
    private Integer elapsedTime;
    private Integer positionsLeft;
    private Date turnLiveTime;

    public void setTurnLiveTime(Date turnLiveTime) {
        this.turnLiveTime = turnLiveTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void setTurnEnum(TurnEnum turnEnum) {
        this.turnEnum = turnEnum;
    }

    public void setAllowedGroups(List<Group> allowedGroups) {
        this.allowedGroups = allowedGroups;
    }

    public void setAllowedCourses(List<Course> allowedCourses) {
        this.allowedCourses = allowedCourses;
    }

    public void setAllowedFaculties(List<Faculty> allowedFaculties) {
        this.allowedFaculties = allowedFaculties;
    }

    public void setAllowedDepartments(List<Department> allowedDepartments) {
        this.allowedDepartments = allowedDepartments;
    }

    public void setPositionsCount(Integer positionsCount) {
        this.positionsCount = positionsCount;
    }

    public void setAllTime(Integer allTime) {
        this.allTime = allTime;
    }

    public void setAverageTime(Integer averageTime) {
        this.averageTime = averageTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setPositionsLeft(Integer positionsLeft) {
        this.positionsLeft = positionsLeft;
    }
}
