package com.eturn.eturn.controller;

import com.eturn.eturn.entity.Counter;
import com.eturn.eturn.service.CounterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/counter")
public class CounterController {
    private final CounterService counterService;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping()
    public List<Counter> getCounters(){
        return counterService.getCounters();
    }
}
