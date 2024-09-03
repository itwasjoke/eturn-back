package com.eturn.eturn.entity;

import com.eturn.eturn.enums.NotifyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "notify")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private NotifyType type;
    private Date created;
    private Long userId;
    private boolean isMany;
}
