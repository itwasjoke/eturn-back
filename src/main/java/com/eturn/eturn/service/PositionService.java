package com.eturn.eturn.service;

import com.eturn.eturn.dto.PositionDTO;
import com.eturn.eturn.dto.PositionMoreInfoDTO;
import com.eturn.eturn.entity.Position;

import java.util.List;
import java.util.Optional;


public interface PositionService {
//    PositionDTO getPositionById(Long id);

    // TODO int pageSize, int pageNumber
//    Optional<Position> getLastPosition(Long idUser, Long idTurn);

    PositionMoreInfoDTO createPositionAndSave(String login, Long idTurn);

    List<PositionDTO> getPositonList(Long idTurn, int page);

    void update(Long id);

    void delete(Long id);

    PositionMoreInfoDTO getFirstUserPosition(Long turnId, String username);
}
