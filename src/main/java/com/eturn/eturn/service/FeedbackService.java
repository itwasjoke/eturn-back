package com.eturn.eturn.service;

import com.eturn.eturn.dto.FeedbackCreateDTO;
import com.eturn.eturn.dto.FeedbackDTO;
import com.eturn.eturn.entity.Feedback;

import java.util.List;

public interface FeedbackService {
    void createFeedback(String username, FeedbackCreateDTO feedbackCreateDTO);
    List<FeedbackDTO> getFeedback(String username, Integer page);
}
