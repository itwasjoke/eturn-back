package com.eturn.eturn.entity;

import com.eturn.eturn.enums.AccessEnum;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Table
@Entity
public class Member {
    // mapstruct
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long idUser;
    private Long idTurn;
    @Enumerated(EnumType.ORDINAL)
    private AccessEnum accessEnum;

    public void setId(Long id) {
        this.id = id;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public void setIdTurn(Long idTurn) {
        this.idTurn = idTurn;
    }

    public void setAccessEnum(AccessEnum accessEnum) {
        this.accessEnum = accessEnum;
    }
}
