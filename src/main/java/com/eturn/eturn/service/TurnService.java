package com.eturn.eturn.service;

import com.eturn.eturn.dto.MemberDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnMoreInfoDTO;
import com.eturn.eturn.entity.Turn;

import java.util.List;
import java.util.Map;
public interface TurnService {
    List<Turn> getAllTurns();
    TurnDTO getTurn(Long id);

    Turn getTurnFrom(Long id);


    List<TurnDTO> getUserTurns(String login, Map<String,String> params);

    Long createTurn(TurnMoreInfoDTO turn, String Login);
    void updateTurn(Long idUser, Turn turn);

    void deleteTurn(String username, Long idTurn);

    void countUser(Turn turn);

    MemberDTO getMember(String username, Long idTurn);

    List<MemberDTO> getMemberList(String username, String type, Long turnId);
    void addTurnToUser(Long turnId, String login, String access);




}
