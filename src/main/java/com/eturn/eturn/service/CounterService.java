package com.eturn.eturn.service;

import com.eturn.eturn.entity.Counter;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CounterService {
    void plusValue(String id);
    List<Counter> getCounters();
}
