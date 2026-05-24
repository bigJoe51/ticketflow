package com.kilimo.ticket.config;

import java.io.IOException;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kilimo.ticket.service.JWTService;
import com.kilimo.ticket.service.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{
    @Autowired
    private JWTService jservice;
    @Autowired
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        String username = null;
        
        // Check Authorization header first
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7);
            username = extractUsernameSafely(token);
        }
        
        // If no token in header, check cookies
        if(token == null && request.getCookies() != null) {
            for(jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if("ticketflow-token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    username = extractUsernameSafely(token);
                    break;
                }
            }
        }

            
        if(username!=null && shouldRefreshAuthentication()){
            UserDetails userDetails=context.getBean(MyUserDetailsService.class).loadUserByUsername(username);
            if(jservice.validateToken(token, userDetails)){
                UsernamePasswordAuthenticationToken authtoken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authtoken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authtoken);
            }
            
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldRefreshAuthentication() {
        var currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuthentication == null) {
            return true;
        }
        if (currentAuthentication instanceof AnonymousAuthenticationToken) {
            return true;
        }
        return currentAuthentication.getAuthorities().stream()
            .map((authority) -> authority.getAuthority())
            .noneMatch((authority) -> authority != null && authority.startsWith("ROLE_"));
    }

    private String extractUsernameSafely(String token) {
        try {
            return jservice.extractUsername(token);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            return null;
        }
    }

}
