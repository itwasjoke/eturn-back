package com.eturn.eturn.additionalService.member;

import com.eturn.eturn.entity.Turn;

public interface MemberNotificationService {
    void notifyReceiptRequest(Turn turn);
}
