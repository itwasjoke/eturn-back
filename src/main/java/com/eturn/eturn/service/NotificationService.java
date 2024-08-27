package com.eturn.eturn.service;

public interface NotificationService {
    void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number);
    void notifyTurnCreated(String token, String turnName);
    void notifyReceiptRequest(String token, String turnName);
}
