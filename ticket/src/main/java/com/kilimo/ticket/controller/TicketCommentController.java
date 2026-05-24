package com.kilimo.ticket.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kilimo.ticket.dto.CommentDTO;
import com.kilimo.ticket.mapper.CommentMapper;
import com.kilimo.ticket.model.TicketComment;
import com.kilimo.ticket.service.TicketCommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService commentService;
    private final CommentMapper commentMapper;

    @PostMapping("/add")
    public CommentDTO addComment(@RequestBody CommentDTO commentDTO){
        TicketComment comment = commentMapper.toEntity(commentDTO);
        TicketComment saved = commentService.addComment(comment);
        return commentMapper.toDTO(saved);
    }

    @GetMapping("/ticket/{ticketId}")
    public List<CommentDTO> getTicketComments(@PathVariable Long ticketId){
        return commentService.getTicketComments(ticketId)
                .stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }
}
