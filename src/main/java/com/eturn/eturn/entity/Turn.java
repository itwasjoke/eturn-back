package com.eturn.eturn.entity;

import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.TurnEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "turn")
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "turn", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Position> positions;

    @OneToMany(mappedBy = "turn", fetch = FetchType.LAZY)
    private Set<Member> memberUsers;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    private TurnEnum turnType;

    @Enumerated(EnumType.STRING)
    private AccessTurnEnum accessTurnType;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "turn_group",
            joinColumns = @JoinColumn(name="turn_id"),
            inverseJoinColumns = @JoinColumn(name="group_id")
    )
    private Set<Group> allowedGroups;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "turn_faculty",
            joinColumns = @JoinColumn(name="turn_id"),
            inverseJoinColumns = @JoinColumn(name="faculty_id")
    )
    private Set<Faculty> allowedFaculties;

    private Date dateStart;

    private Date dateEnd;

    private int countUsers;

    private int allowedTime;

//    private Integer countPositions;
//    private Integer allTime;
//    private Integer averageTime;
//    private Integer elapsedTime;
//    private Integer positionsLeft;
//    private Date turnLiveTime;

}
