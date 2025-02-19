package com.eturn.eturn.additionalService.position;

import com.eturn.eturn.entity.Turn;

public interface PositionTimerService {
    void deleteOverdueElements(Turn turn);
    void startTimerForNewFirstPosition(Turn turn);
}
