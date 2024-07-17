package com.eturn.eturn.service;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;

import java.util.List;


public interface PositionService {
//    PositionDTO getPositionById(Long id);

    // TODO int pageSize, int pageNumber
//    Optional<Position> getLastPosition(Long idUser, Long idTurn);

    PositionMoreInfoDTO createPositionAndSave(String login, Long idTurn);

    List<PositionDTO> getPositionList(Long idTurn, int page);

    void update(Long id, String username);

    void delete(Long id, String username);

    void deleteMember(long id, String username);
    void changeMemberStatus(long id, String type, String username);

    PositionMoreInfoDTO getFirstUserPosition(Long turnId, String username);

    PositionMoreInfoDTO getFirstPosition(Long turnId, String username);

    void addTurnToUser(User user, Turn turn);
}
