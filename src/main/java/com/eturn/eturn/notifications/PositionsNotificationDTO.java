package com.eturn.eturn.notifications;

import com.eturn.eturn.entity.User;

import java.util.List;

public record PositionsNotificationDTO(List<User> userList, String turnName) {
}
