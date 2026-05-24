package com.kilimo.ticket.mapper;

import org.springframework.stereotype.Component;

import com.kilimo.ticket.dto.UserProfileDTO;
import com.kilimo.ticket.model.User;

@Component
public class UserMapper {

    public UserProfileDTO toDTO(User user){

        UserProfileDTO dto = new UserProfileDTO();

        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());

        if(user.getRole() != null){
            dto.setRole(user.getRole().getName());
        }

        dto.setStatus(user.getStatus());
        dto.setProfileImageUrl(user.getProfileImageUrl());

        return dto;
    }
}
