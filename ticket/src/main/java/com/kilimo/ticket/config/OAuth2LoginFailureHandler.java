package com.kilimo.ticket.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.base-url:http://localhost:3000}")
    private String appBaseUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String raw = exception == null ? "unknown_oauth_error" : exception.getMessage();
        String encoded = URLEncoder.encode(raw == null ? "unknown_oauth_error" : raw, StandardCharsets.UTF_8);
        response.sendRedirect(appBaseUrl.replaceAll("/+$", "") + "/login?error=google_auth_failed&reason=" + encoded);
    }
}
