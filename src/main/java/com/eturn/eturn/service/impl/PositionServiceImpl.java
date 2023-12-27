package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.PositionsDTO;
import com.eturn.eturn.dto.mapper.PositionsListMapper;
import com.eturn.eturn.dto.mapper.PositionsMapper;
import com.eturn.eturn.dto.mapper.TurnMapper;
import com.eturn.eturn.entity.*;
import com.eturn.eturn.repository.PositionRepository;
import com.eturn.eturn.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;
    private final UserService userService;
    private final PositionsListMapper positionsListMapper;
    private final TurnService turnService;
    private final TurnMapper turnMapper;
    private final PositionsMapper positionMapper;
    private final MemberService memberService;



    public PositionServiceImpl(PositionRepository positionRepository, UserService userService,
                               PositionsListMapper positionsListMapper, TurnService turnService,
                               TurnMapper turnMapper, PositionsMapper positionMapper, MemberService memberService) {
        this.positionRepository = positionRepository;
        this.userService = userService;
        this.positionsListMapper=positionsListMapper;
        this.turnService=turnService;
        this.turnMapper=turnMapper;
        this.positionMapper = positionMapper;
        this.memberService = memberService;

    }

    @Override
    public PositionsDTO getPositionById(Long id) {
        return positionMapper.positionToPositionDTO(positionRepository.getReferenceById(id));
    }

    @Override
    // TODO int pageSize, int pageNumber
    public Optional<Position> getLastPosition(Long idUser, Long idTurn) {
        // TODO positionRepository.findAllByUser(new User(), Pageable.ofSize(pageSize).withPage(pageNumber));
        User user = userService.getUserFrom(idUser);
        Turn turn=turnService.getTurnFrom(idTurn);
        return positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user,turn);
    }

    @Override
    public void createPositionAndSave(Long idUser,
                                      Long idTurn) {
        Turn turn = turnService.getTurnFrom(idTurn);
        User user=userService.getUserFrom(idUser);

        List<Member> members =memberService.getListMemeberTurn(idTurn);

        int permitedCountPeople = 5;

        Optional<Position> ourPosition = positionRepository.findFirstByUserAndTurnOrderByNumberDesc(user,turn);
        Optional<Position> lastPositionInTurn =positionRepository.findTopByTurnOrderByNumberDesc(turn);

        int lastNumber= lastPositionInTurn.isPresent()?lastPositionInTurn.get().getNumber():0;
        int ourUserLastNumber=ourPosition.isPresent()?ourPosition.get().getNumber():0;

        if (members.size()<permitedCountPeople) {
            if(ourPosition.isPresent()){
                //тут ошбка должно быть
            }
            else {
               Position newPosition= new Position();
               newPosition.setStarted(false);
               newPosition.setUser(user);
               newPosition.setTurn(turn);
               newPosition.setNumber(lastNumber+1);
               positionRepository.save(newPosition);

        }
        }
        else{

            if(ourPosition.isPresent()){
                if (lastNumber-ourUserLastNumber>permitedCountPeople) {
                    Position newPosition= new Position();
                    newPosition.setStarted(false);
                    newPosition.setUser(user);
                    newPosition.setTurn(turn);
                    newPosition.setNumber(lastNumber+1);
                    positionRepository.save(newPosition);

                }
            }
            else {
                Position newPosition= new Position();
                newPosition.setStarted(false);
                newPosition.setUser(user);
                newPosition.setTurn(turn);
                newPosition.setNumber(lastNumber+1);
                positionRepository.save(newPosition);

            }
        }


    }

    @Override
    public List<PositionsDTO> getPositonList(Long idTurn, int page, int size){
        Turn turn=turnService.getTurnFrom(idTurn);
        Pageable paging = PageRequest.of(page,size);
        Page<Position> positions =positionRepository.findAllByTurn(turn,paging);



        return positionsListMapper.map(positions);
        //тут ошибку можно бросить
    }

    @Override
    public void update(Long id, boolean started){
        Position position=positionRepository.getReferenceById(id);
        position.setStarted(started);
        positionRepository.save(position);
    }
    @Override
    public void delete(Long id){
        Position position = positionRepository.getReferenceById(id);
        positionRepository.delete(position);
    }
}
