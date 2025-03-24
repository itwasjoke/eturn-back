package com.eturn.eturn.service.impl;

import com.eturn.eturn.additionalService.turn.TurnRepositoryService;
import com.eturn.eturn.dto.StatisticDTO;
import com.eturn.eturn.dto.TurnForListDTO;
import com.eturn.eturn.entity.Counter;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.exception.user.AccessException;
import com.eturn.eturn.service.CounterService;
import com.eturn.eturn.service.StatisticService;
import com.eturn.eturn.service.TurnService;
import com.eturn.eturn.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {
    private final UserService userService;
    private final TurnRepositoryService turnRepositoryService;
    private final CounterService counterService;


    public StatisticServiceImpl(
            TurnService turnService,
            UserService userService,
            TurnRepositoryService turnRepositoryService,
            CounterService counterService
    ) {
        this.userService = userService;
        this.turnRepositoryService = turnRepositoryService;
        this.counterService = counterService;
    }

    /**
     * Получение статистики основной
     * @param username имя пользователя
     * @return DTO c недавней информацией
     */
    @Override
    public StatisticDTO getStatistic(String username) {
        User user = userService.getUserFromLogin(username);
        if (user.getRole() != Role.ADMIN){
            throw new AccessException("no admin access");
        }

        List<Counter> counters = counterService.getCounters();
        List<TurnForListDTO> turns =
                turnRepositoryService.getStatisticForRecentTurns();
        List<String> users = userService.getStatisticForRecentUsers();
        return new StatisticDTO(counters, turns, users);
    }
}
