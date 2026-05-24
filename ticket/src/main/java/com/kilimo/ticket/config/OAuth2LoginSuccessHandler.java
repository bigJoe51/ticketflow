package com.kilimo.ticket.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.kilimo.ticket.model.User;
import com.kilimo.ticket.service.JWTService;
import com.kilimo.ticket.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JWTService jwtService;

    @Value("${app.base-url:http://localhost:3000}")
    private String appBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = String.valueOf(oauthUser.getAttributes().getOrDefault("email", ""));
        String name = String.valueOf(oauthUser.getAttributes().getOrDefault("name", "TicketFlow User"));

        if (email.isBlank()) {
            response.sendRedirect(frontendLoginUrl() + "?error=google_email_missing");
            return;
        }

        User user = userService.findOrCreateGoogleUser(email, name);
        String token = jwtService.generateToken(user.getEmail());

        Cookie cookie = new Cookie("ticketflow-token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();

        String redirect = frontendLoginUrl() + "?oauth=1&token=" + url(token)
            + "&email=" + url(user.getEmail())
            + "&firstName=" + url(user.getFirstName())
            + "&lastName=" + url(user.getLastName())
            + "&role=" + url(user.getRole() != null ? user.getRole().getName() : "USER")
            + "&userId=" + url(user.getId() == null ? "" : user.getId().toString());
        response.sendRedirect(redirect);
    }

    private String frontendLoginUrl() {
        return appBaseUrl.replaceAll("/+$", "") + "/login";
    }

    private String url(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
