package com.eturn.eturn.domain;

import jakarta.persistence.*;

@Entity
@Table
public class AllowGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long idTurn;
    private Long idGroup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdTurn() {
        return idTurn;
    }

    public void setIdTurn(Long idTurn) {
        this.idTurn = idTurn;
    }

    public Long getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(Long idGroup) {
        this.idGroup = idGroup;
    }
}
