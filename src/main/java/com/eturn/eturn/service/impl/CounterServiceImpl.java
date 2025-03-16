package com.eturn.eturn.service.impl;

import com.eturn.eturn.entity.Counter;
import com.eturn.eturn.repository.CounterRepository;
import com.eturn.eturn.service.CounterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CounterServiceImpl implements CounterService {

    private final CounterRepository counterRepository;

    public CounterServiceImpl(CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    /**
     * Добавление элемента к счетчику
     * @param id название счетчика
     */
    @Override
    public void plusValue(String id) {
        Optional<Counter> counterOptional = counterRepository.findCounterById(id);
        if (counterOptional.isPresent()) {
            Counter counter = counterOptional.get();
            counter.setValue(counter.getValue() + 1);
            counterRepository.save(counter);
        } else {
            Counter counter = new Counter();
            counter.setId(id);
            counter.setValue(1);
            counterRepository.save(counter);
        }

    }

    /**
     * Получение всех счетчиков
     * @return все счётчики
     */
    @Override
    public List<Counter> getCounters() {
        return counterRepository.findAll();
    }
}
