package com.kilimo.ticket.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kilimo.ticket.model.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class VerificationService {
    private final AuthenticationManager authManager;
    private final JWTService jservice;
    
    public String verify(User guy) {
        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(guy.getEmail(), guy.getPassword())
        );
        if (authentication.isAuthenticated()) {
            return jservice.generateToken(guy.getEmail());
        }
        else {
            return "failed";
        }
    }    
}
