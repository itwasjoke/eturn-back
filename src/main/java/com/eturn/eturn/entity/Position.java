package com.eturn.eturn.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Position {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(targetEntity = User.class, cascade = CascadeType.ALL)
    private User user;

    private int number;

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
