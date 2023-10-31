package com.eturn.eturn.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;


@Entity
@Table
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Getter
    @Column(updatable = false)
    private Long idTurn;
    @Getter
    @Column(updatable = false)
    private Long idUser;
    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime creationDate;

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Long getId() {
        return id;
    }

    public void setIdTurn(Long idTurn) {
        this.idTurn = idTurn;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }
}
