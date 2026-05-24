package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.RatingDTO;
import com.kilimo.ticket.mapper.RatingMapper;
import com.kilimo.ticket.model.TicketRating;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.TicketRatingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class TicketRatingController {

    private final TicketRatingService ratingService;
    private final RatingMapper ratingMapper;

    @PostMapping("/rate")
    public RatingDTO rateTicket(@RequestBody RatingDTO ratingDTO){
        TicketRating rating = ratingMapper.toEntity(ratingDTO);
        TicketRating saved = ratingService.rateTicket(rating);
        return ratingMapper.toDTO(saved);
    }

    @GetMapping("/staff/{staffId}")
    public Double getStaffAverageRating(@PathVariable Long staffId){
        User staff = new User();
        staff.setId(staffId);
        return ratingService.getStaffAverageRating(staff);
    }

    @GetMapping("/staff/{staffId}/details")
    public List<RatingDTO> getStaffRatings(@PathVariable Long staffId) {
        return ratingService.getRatingsForStaff(staffId)
            .stream()
            .map(ratingMapper::toDTO)
            .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<RatingDTO> getAllRatings() {
        return ratingService.getAllRatings()
            .stream()
            .map(ratingMapper::toDTO)
            .collect(Collectors.toList());
    }
}
