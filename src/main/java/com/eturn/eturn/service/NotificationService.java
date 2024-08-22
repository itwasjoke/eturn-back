package com.eturn.eturn.service;

import org.springframework.stereotype.Service;

import java.util.List;

public interface NotificationService {
    void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number);
    void notifyTurnCreated(List<String> tokens, String turnName);
    void notifyReceiptRequest(List<String> tokens);
}
