package com.eturn.eturn.controller;

import com.eturn.eturn.dto.FeedbackCreateDTO;
import com.eturn.eturn.dto.FeedbackDTO;
import com.eturn.eturn.service.FeedbackService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value ="/feedback", produces = "application/json; charset=utf-8")
@Tag(name = "Обратная связь", description = "Работа над получением мнения пользователей")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping()
    public void addFeedback(
            HttpServletRequest request,
            @Parameter(
                    name = "feedbackCreateDTO",
                    description = "Обратная связь"
            )
            @RequestBody FeedbackCreateDTO feedbackCreateDTO
            ) {
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        feedbackService.createFeedback(
                userDetails.getUsername(),
                feedbackCreateDTO
        );
    }

    @GetMapping()
    public List<FeedbackDTO> getFeedbackList(
            HttpServletRequest request,
            @RequestParam @Parameter(name = "page", description = "Номер страницы") Integer page
    ){
        var authentication = (Authentication) request.getUserPrincipal();
        var userDetails = (UserDetails) authentication.getPrincipal();
        return feedbackService.getFeedback(userDetails.getUsername(), page);
    }
}
