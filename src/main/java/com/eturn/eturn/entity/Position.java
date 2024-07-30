package com.eturn.eturn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@NamedNativeQuery(name = "deletePositionSQL", query = "delete from positions where turn_id = :turnId and number <= :number ", resultClass = Void.class)
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

    private boolean start;

    private int number;

    private Date dateEnd;

    private Date dateStart;

    private int skipCount;
}
