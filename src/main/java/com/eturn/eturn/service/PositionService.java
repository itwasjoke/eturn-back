package com.eturn.eturn.service;

import com.eturn.eturn.dto.PositionsDTO;
import com.eturn.eturn.entity.Position;
import com.eturn.eturn.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface PositionService {
    PositionsDTO getPositionById(Long id);

    // TODO int pageSize, int pageNumber
    Optional<Position> getLastPosition(Long idUser, Long idTurn);

    void createPositionAndSave(Long idUser, Long idTurn);

    List<PositionsDTO> getPositonList(Long idTurn, int size, int page);

    void update(Long id, boolean started);

    void delete(Long id);
}
