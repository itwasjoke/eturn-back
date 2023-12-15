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

    private Long idUser;

    private Long idTurn;

    @Enumerated(EnumType.STRING)
    private AccessMemberEnum accessMemberEnum;
}
