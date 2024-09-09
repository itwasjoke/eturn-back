package com.eturn.eturn.service;

import com.eturn.eturn.enums.NotifySendType;

public interface NotificationService {
    void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number);
    void notifyTurnCreated(String token, String turnName, NotifySendType sendType);
    void notifyReceiptRequest(String token, String turnName, NotifySendType sendType);
}
