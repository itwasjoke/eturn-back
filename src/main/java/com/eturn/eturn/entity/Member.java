package com.eturn.eturn.entity;

import com.eturn.eturn.enums.AccessMemberEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "turn_id")
    private Turn turn;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Position> positionsMember;


    @Enumerated(EnumType.STRING)
    private AccessMemberEnum accessMemberEnum;

    private boolean invited = false;

    private boolean invitedForTurn = false;
}
