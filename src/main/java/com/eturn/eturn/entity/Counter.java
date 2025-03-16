package com.eturn.eturn.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Counter {
    @Id
    String id;

    Integer value;
}
