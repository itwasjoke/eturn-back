package com.eturn.eturn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@NamedNativeQuery(name = "getPositionForDelete", query = "SELECT p.skip_count, p.start, p.date_end, p.date_start, p.id, p.member_id, p.turn_id, p.user_id, p.group_name FROM positions AS p WHERE p.turn_id = :turn ORDER BY p.id DESC OFFSET :count LIMIT 1 ", resultSetMapping = "PositionMapping")
@NamedNativeQuery(name = "getPositionForDeleteOverdueElements", query = "SELECT p.skip_count, p.start, p.date_end, p.date_start, p.id, p.member_id, p.turn_id, p.user_id, p.group_name FROM positions AS p WHERE p.turn_id = :turn ORDER BY p.id OFFSET :count LIMIT 1 ", resultSetMapping = "PositionMapping")
@SqlResultSetMapping(
        name = "PositionMapping",
        entities =
        @EntityResult(
                entityClass = Position.class
        )
)
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="turn_id", nullable = false)
    private Turn turn;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private String groupName;

    private Boolean start;

    private Date dateEnd;

    private Date dateStart;

    private Integer skipCount;

    public boolean isStart() {
        return start;
    }
}
