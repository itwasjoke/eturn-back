package com.eturn.eturn.notifications;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDTO {
    Long turnId;
    Integer type;
    Long groupId;
    String turnName;
}
