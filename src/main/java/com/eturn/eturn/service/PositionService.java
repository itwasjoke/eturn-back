package com.eturn.eturn.service;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;

import java.util.List;


public interface PositionService {
//    PositionDTO getPositionById(Long id);

    // TODO int pageSize, int pageNumber
//    Optional<Position> getLastPosition(Long idUser, Long idTurn);

    PositionMoreInfoDTO createPositionAndSave(String login, Long idTurn);

    List<PositionDTO> getPositionList(Long idTurn, int page);

    void update(Long id, String username);

    void delete(Long id, String username);

    PositionMoreInfoDTO getFirstUserPosition(Long turnId, String username);

    PositionMoreInfoDTO getFirstPosition(Long turnId, String username);
}
