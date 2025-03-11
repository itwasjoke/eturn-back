package com.eturn.eturn.service.impl;

import com.eturn.eturn.dto.FeedbackCreateDTO;
import com.eturn.eturn.dto.FeedbackDTO;
import com.eturn.eturn.dto.mapper.FeedbackListMapper;
import com.eturn.eturn.entity.Feedback;
import com.eturn.eturn.entity.User;
import com.eturn.eturn.enums.Role;
import com.eturn.eturn.exception.user.AccessException;
import com.eturn.eturn.repository.FeedbackRepository;
import com.eturn.eturn.service.FeedbackService;
import com.eturn.eturn.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserService userService;
    private final FeedbackListMapper feedbackListMapper;

    public FeedbackServiceImpl(
            FeedbackRepository feedbackRepository,
            UserService userService,
            FeedbackListMapper feedbackListMapper
    ) {
        this.feedbackRepository = feedbackRepository;
        this.userService = userService;
        this.feedbackListMapper = feedbackListMapper;
    }

    /**
     * Создание обратной связи
     * @param username имя пользователя
     * @param feedbackCreateDTO форма с данными
     */
    @Override
    public void createFeedback(
            String username,
            FeedbackCreateDTO feedbackCreateDTO
    ) {
        User user = userService.getUserFromLogin(username);
        if (feedbackRepository.existsByUser(user)){
            throw new AccessException("already exists");
        }
        Feedback feedback = new Feedback();
        feedback.setDate(new Date());
        feedback.setEvaluation(feedbackCreateDTO.evaluation());
        feedback.setText(feedbackCreateDTO.text());
        feedback.setUser(user);
        feedbackRepository.save(feedback);
    }

    /**
     * Возвращение списка со всеми отзывами
     * @param username имя пользователя
     * @param page страница
     * @return список
     */
    @Override
    public List<FeedbackDTO> getFeedback(
            String username,
            Integer page
    ) {
        User user = userService.getUserFromLogin(username);
        if (user.getRole()!= Role.ADMIN){
            throw new AccessException("No admin access");
        }
        Pageable pageable = PageRequest.of(
                page,
                20,
                Sort.by("date").descending()
        );
        Page<Feedback> pageFeedback = feedbackRepository.findAll(pageable);
        return feedbackListMapper.mapFeedback(pageFeedback.toList());

    }
}
