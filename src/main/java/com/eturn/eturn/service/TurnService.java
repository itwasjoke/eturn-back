package com.eturn.eturn.service;

import com.eturn.eturn.dto.*;
import com.eturn.eturn.entity.Turn;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
public interface TurnService {

    Turn getTurnFrom(String hash);

    List<TurnForListDTO> getUserTurns(String login, Map<String,String> params);

    String createTurn(TurnCreatingDTO turn, String Login);
    void deleteTurn(String username, String hash);
    void saveTurn(Turn turn);

    List<TurnForListDTO> getLinkedTurn(String hash, String username);
    List<MemberDTO> getMemberList(String username, String type, String hash, int page);
    List<MemberDTO> getUnconfMemberList(String username, String type, String hash);
    void changeTurn(TurnEditDTO turn, String username);
}
