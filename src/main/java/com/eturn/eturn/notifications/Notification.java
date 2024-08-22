package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Notification {
    Long turnId;
    Integer type;
    List<Long> groupList;
}
