package com.eturn.eturn.additionalService.position;

import com.eturn.eturn.notifications.PositionsNotificationDTO;

public interface PositionNotificationService {
    PositionsNotificationDTO getPositionsForNotify(Long turnId);
}
