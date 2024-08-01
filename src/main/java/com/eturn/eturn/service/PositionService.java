package com.eturn.eturn.service;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.entity.Member;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;

import java.util.List;


public interface PositionService {
//    PositionDTO getPositionById(Long id);

    // TODO int pageSize, int pageNumber
//    Optional<Position> getLastPosition(Long idUser, Long idTurn);

    PositionMoreInfoDTO createPositionAndSave(String login, String hash);

    PositionsTurnDTO getPositionList(String hash, String username, int page);

    void update(Long id, String username);

    void delete(Long id, String username);

    void deleteMember(long id, String username);
    void changeMemberStatus(long id, String type, String username);

    void skipPosition(long id, String username);

    PositionMoreInfoDTO getFirstUserPosition(String hash, String username);

    PositionMoreInfoDTO getFirstPosition(String hash, String username);

    Member addTurnToUser(User user, Turn turn);

    TurnDTO getTurn(String hash, String login);
    int count(String hash);
    void inviteUser(String hash, String username);
    void changeMemberInvite(Long id, boolean status);

}
