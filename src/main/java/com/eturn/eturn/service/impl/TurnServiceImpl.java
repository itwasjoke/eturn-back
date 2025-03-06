package com.eturn.eturn.service.impl;

import com.eturn.eturn.additionalService.turn.TurnCreationService;
import com.eturn.eturn.additionalService.turn.TurnRepositoryService;
import com.eturn.eturn.additionalService.turn.TurnUpdateService;
import com.eturn.eturn.dto.TurnCreatingDTO;
import com.eturn.eturn.dto.TurnDTO;
import com.eturn.eturn.dto.TurnEditDTO;
import com.eturn.eturn.dto.TurnForListDTO;
import com.eturn.eturn.entity.Turn;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.exception.turn.LocalNotFoundTurnException;
import com.eturn.eturn.repository.TurnRepository;
import com.eturn.eturn.service.TurnService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class TurnServiceImpl implements TurnService {

    private static Logger logger = LogManager.getLogger(TurnServiceImpl.class);
    private final TurnRepository turnRepository;
    private TurnRepositoryService turnRepositoryService;
    private final TurnUpdateService turnUpdateService;
    private TurnCreationService turnCreationService;

    public TurnServiceImpl(
            TurnRepository turnRepository,
            TurnUpdateService turnUpdateService
    ) {
        this.turnRepository = turnRepository;
        this.turnUpdateService = turnUpdateService;
    }

    @Autowired
    public void setTurnRepositoryService(
            TurnRepositoryService turnRepositoryService
    ){
        this.turnRepositoryService = turnRepositoryService;
    }
    @Autowired
    public void setTurnCreationService(
            TurnCreationService turnCreationService
    ){
        this.turnCreationService = turnCreationService;
    }

    @Override
    public Turn getTurnFrom(String hash) {
        Optional<Turn> turn = turnRepository.findTurnByHash(hash);
        if(turn.isPresent()){
            return turn.get();
        }
        else{
            throw new LocalNotFoundTurnException("No turn in database on getTurnFrom method (TurnServiceImpl.java)");
        }
    }

    /**
     * Получение очередей пользователя
     * @param login имя пользователя
     * @param params учебная/организационная мои/доступные
     * @return список очередей
     */
    @Transactional
    @Override
    public List<TurnForListDTO> getUserTurns(
            String login,
            Map<String, String> params
    ) {
        return turnRepositoryService.getUserTurns(
                login,
                params
        );
    }

    /**
     * Получаем текущую очередь
     * @param hash хэш очереди
     * @param login имя пользователя
     * @return очередь с подробной информацией
     */
    @Override
    public TurnDTO getTurn(
            String hash,
            String login
    ) {
        return  turnRepositoryService.getTurn(
                hash,
                login
        );
    }

    /**
     * Создание очереди
     * @param turnDTO тело очереди с параметрами
     * @param login имя пользователя
     * @return хэш очереди
     */
    @Override
    @Transactional
    public String createTurn(
            TurnCreatingDTO turnDTO,
            String login
    ) {
        return turnCreationService.createTurn(
                turnDTO,
                login
        );
    }

    /**
     * Удаление очереди
     * @param username имя пользователя
     * @param hash хэш очереди
     */
    @Override
    @Transactional
    public void deleteTurn(
            String username,
            String hash
    ) {
        turnUpdateService.deleteTurn(
                username,
                hash
        );
    }

    /**
     * Сохранение информации об очереди
     * @param turn очередь
     */
    @Override
    public void saveTurn(Turn turn) {
        turnRepository.save(turn);
    }

    /**
     * Количество очередей у пользователя
     * для проверки, что можно создать больше очередей
     * @param user пользователь
     * @return количество
     */
    @Override
    public int getCountTurnsOfUser(User user) {
        return turnRepository.countAllByCreator(user);
    }

    /**
     * Получение очереди при поиске по ссылке
     * @param hash хэш очереди
     * @param username пользователь
     * @return
     */
    @Override
    public List<TurnForListDTO> getLinkedTurn(
            String hash,
            String username
    ) {
        return turnRepositoryService.getLinkedTurn(
                hash,
                username
        );
    }

    /**
     * Изменение очереди
     * @param turn очередь с изменениями
     * @param username пользователь
     */
    @Override
    public void changeTurn(
            TurnEditDTO turn,
            String username
    ) {
        turnUpdateService.updateTurn(
                turn,
                username
        );
    }

    /**
     * Добавление часа к концу очереди
     * @param hash хэш очереди
     * @param username пользователь
     */
    @Override
    public void addHour(
            String hash,
            String username
    ) {
        turnUpdateService.addHour(hash, username);
    }

}
