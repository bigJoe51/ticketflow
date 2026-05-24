package com.kilimo.ticket.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kilimo.ticket.dao.UserRepository;
import com.kilimo.ticket.model.User;
import com.kilimo.ticket.model.UserPrincipal;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repo;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = repo.findByEmail(email);
        
        if (!user.isPresent()) {
            throw new UsernameNotFoundException("email not found in db: " + email);
        }
        
        return new UserPrincipal(user.get());
    }
}
