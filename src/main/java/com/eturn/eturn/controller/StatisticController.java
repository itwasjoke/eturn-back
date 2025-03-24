package com.eturn.eturn.controller;

import com.eturn.eturn.dto.StatisticDTO;
import com.eturn.eturn.entity.Counter;
import com.eturn.eturn.service.CounterService;
import com.eturn.eturn.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@Tag(name = "Статистика", description = "Получение информации для администраторов")
public class StatisticController {

    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping()
    @Operation(
            summary = "Получение основной статистики",
            description = "Только для администраторов"
    )
    public StatisticDTO getStatistics(HttpServletRequest request) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return statisticService.getStatistic(userDetails.getUsername());
    }
}
