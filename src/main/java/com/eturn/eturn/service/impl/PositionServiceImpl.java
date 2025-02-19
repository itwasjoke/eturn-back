package com.eturn.eturn.service.impl;

import com.eturn.eturn.additionalService.position.*;
import com.eturn.eturn.dto.*;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.notifications.PositionsNotificationDTO;
import com.eturn.eturn.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PositionServiceImpl implements PositionService {
    private final PositionCreationService positionCreationService;
    private final PositionDeletionService positionDeletionService;
    private final PositionUpdateService positionUpdateService;
    private final PositionRepositoryService positionRepService;
    private final PositionNotificationService positionNotificationService;

    @Autowired
    public PositionServiceImpl(
            PositionCreationService positionCreationService,
            PositionDeletionService positionDeletionService,
            PositionUpdateService positionUpdateService,
            PositionRepositoryService positionRepService,
            PositionNotificationService positionNotificationService
    ) {
        this.positionCreationService = positionCreationService;
        this.positionDeletionService = positionDeletionService;
        this.positionUpdateService = positionUpdateService;
        this.positionRepService = positionRepService;
        this.positionNotificationService = positionNotificationService;
    }

    @Override
    @Transactional
    public DetailedPositionDTO createPositionAndSave(String login, String hash) {
        return positionCreationService.createPositionAndSave(login, hash);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        positionDeletionService.delete(id, username);
    }

    @Override
    @Transactional
    public void update(Long id, String username, String status) {
        positionUpdateService.update(id, username, status);
    }

    @Override
    @Transactional
    public void skipPosition(long id, String username) {
        positionUpdateService.skipPosition(id, username);
    }

    @Override
    @Transactional
    public PositionsTurnDTO getPositionList(String hash, String username, int page) {
        return positionRepService.getPositionList(hash, username, page);
    }

    @Override
    public DetailedPositionDTO getFirstUserPosition(String hash, String username) {
        return positionRepService.getFirstUserPosition(hash, username);
    }

    @Override
    public DetailedPositionDTO getFirstPosition(String hash, String username) {
        return positionRepService.getFirstPosition(hash, username);
    }

    @Override
    public PositionsNotificationDTO getPositionsForNotify(Long turnId) {
        return positionNotificationService.getPositionsForNotify(turnId);
    }

    @Override
    public long countPositionsByTurn(Turn turn) {
        return positionRepService.countPositionsByTurn(turn);
    }

    @Override
    public boolean existsAllByTurnAndUser(Turn turn, User user) {
        return positionRepService.existsAllByTurnAndUser(turn, user);
    }

    @Override
    public void deleteAllByTurnAndUser(Turn turn, User user) {
        positionDeletionService.deleteAllByTurnAndUser(turn, user);
    }
}
