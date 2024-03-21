package com.eturn.eturn.entity;

import com.eturn.eturn.enums.AccessMemberEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member")
public class Member {
    // mapstruct
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "turn_id")
    private Turn turn;


    @Enumerated(EnumType.STRING)
    private AccessMemberEnum accessMemberEnum;
}
