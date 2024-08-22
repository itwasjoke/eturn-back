package com.eturn.eturn.service.impl.notifications;

import com.eturn.eturn.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuStoreNotifyServiceImpl implements NotificationService {
    @Override
    public void notifyUserOfTurnPositionChange(String tokenFirst, String turnName, int number) {

    }

    @Override
    public void notifyTurnCreated(List<String> tokens, String turnName) {

    }

    @Override
    public void notifyReceiptRequest(List<String> tokens) {

    }
}
