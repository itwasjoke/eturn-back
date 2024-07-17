package com.eturn.eturn.entity;

import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.enums.AccessTurnEnum;
import com.eturn.eturn.enums.TurnEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@NamedNativeQuery(name = "getMemberOutTurns", query = "SELECT m.access_member_enum, t.id, t.name, t.description, t.user_id, t.date_start, t.date_end, t.count_users, t.timer, t.position_count, t.tags " +
        "FROM turn AS t " +
        "INNER JOIN turn_faculty AS tf ON t.id = tf.turn_id " +
        "LEFT JOIN member AS m ON (t.id = m.turn_id AND m.user_id = :userId) " +
        "WHERE " +
        "t.access_turn_type != 'FOR_LINK' " +
        "AND tf.faculty_id = :facultyId " +
        "AND m.access_member_enum IS NULL " +
        "OR (m.user_id = :userId AND m.access_member_enum = 'INVITED') " +
        "UNION " +
        "SELECT m.access_member_enum, t.id, t.name, t.description, t.user_id, t.date_start, t.date_end, t.count_users, t.timer, t.position_count, t.tags " +
        "FROM turn AS t " +
        "INNER JOIN turn_group AS tg ON t.id = tg.turn_id " +
        "LEFT JOIN member AS m ON (t.id = m.turn_id AND m.user_id = :userId)" +
        "WHERE " +
        "t.access_turn_type != 'FOR_LINK' " +
        "AND t.turn_type = :turnType " +
        "AND tg.group_id = :groupId " +
        "AND m.access_member_enum IS NULL " +
        "OR (m.user_id = :userId AND m.access_member_enum = 'INVITED') ", resultSetMapping = "TurnMapping")
@NamedNativeQuery(name = "getMemberInTurns", query = "SELECT m.access_member_enum, t.id, t.name, t.description, t.user_id, t.date_start, t.date_end, t.count_users, t.timer, t.position_count, t.tags " +
        "FROM turn AS t " +
        "LEFT JOIN member AS m ON (t.id = m.turn_id AND m.user_id = :userId) " +
        "WHERE m.user_id = :userId AND t.turn_type = :turnType AND m.access_member_enum != 'INVITED'" +
        "AND m.access_member_enum != 'BLOCKED' " +
        "AND m.access_member_enum != 'UNCONFIRMED' " +
        "AND m.access_member_enum != 'REFUSED' ", resultSetMapping = "TurnMapping")
@SqlResultSetMapping(
        name = "TurnMapping",
        entities =
        @EntityResult(
                entityClass = Turn.class,
                fields = {
                        @FieldResult(name = "id", column = "id"),
                        @FieldResult(name = "name", column = "name"),
                        @FieldResult(name = "description", column = "description"),
                        @FieldResult(name = "creator", column = "user_id"),
                        @FieldResult(name = "dateStart", column = "date_start"),
                        @FieldResult(name = "dateEnd", column = "date_end"),
                        @FieldResult(name = "countUsers", column = "count_users"),
                        @FieldResult(name = "tags", column = "tags"),
                        @FieldResult(name = "timer", column = "timer"),
                        @FieldResult(name = "positionCount", column = "position_count")
                }
        ),
        columns = @ColumnResult(name = "access_member_enum", type = String.class)
)
@Table(name = "turn")
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "turn", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Position> positions;

    @OneToMany(mappedBy = "turn", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Member> memberUsers;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    private User creator;

    @Enumerated(EnumType.STRING)
    private TurnEnum turnType;

    @Enumerated(EnumType.STRING)
    private AccessTurnEnum accessTurnType;

    @ManyToMany()
    @JoinTable(
            name = "turn_group",
            joinColumns = @JoinColumn(name="turn_id"),
            inverseJoinColumns = @JoinColumn(name="group_id")
    )
    private Set<Group> allowedGroups;

    @ManyToMany()
    @JoinTable(
            name = "turn_faculty",
            joinColumns = @JoinColumn(name="turn_id"),
            inverseJoinColumns = @JoinColumn(name="faculty_id")
    )
    private Set<Faculty> allowedFaculties;

    private Date dateStart;

    private Date dateEnd;

    private int countUsers;

    private int timer;

    private int positionCount;

    private String tags;
    // Среднее время в миллисекундах

    private double smoothedValue;

    private long totalTime = 0;

    private int averageTime = 0;

    private int countPositionsLeft = 0;

}
