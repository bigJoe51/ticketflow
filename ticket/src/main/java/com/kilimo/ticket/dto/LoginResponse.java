package com.kilimo.ticket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private String token;
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String department;
    private String profilePicture;
}
