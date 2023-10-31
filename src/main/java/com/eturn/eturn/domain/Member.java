package com.eturn.eturn.domain;

import jakarta.persistence.*;

@Entity
@Table
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long idUser;
    private Long idTurn;
    private int root;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getIdTurn() {
        return idTurn;
    }

    public void setIdTurn(Long idTurn) {
        this.idTurn = idTurn;
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }



}
