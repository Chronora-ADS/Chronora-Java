package br.com.senai.controller;

import br.com.senai.model.DTO.ReviewDTO;
import br.com.senai.model.entity.ReviewEntity;
import br.com.senai.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/submit/{serviceId}")
    public ResponseEntity<ReviewEntity> submitReview(
            @RequestHeader("Authorization") String tokenHeader,
            @PathVariable Long serviceId,
            @RequestBody @Valid ReviewDTO reviewDTO
    ) {
        ReviewEntity review = reviewService.submitReview(serviceId, reviewDTO, tokenHeader);
        return ResponseEntity.ok(review);
    }
}
