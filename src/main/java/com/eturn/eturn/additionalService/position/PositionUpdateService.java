package com.eturn.eturn.additionalService.position;

public interface PositionUpdateService {
    void update(Long id, String username, String status);
    void skipPosition(long id, String username);
}
