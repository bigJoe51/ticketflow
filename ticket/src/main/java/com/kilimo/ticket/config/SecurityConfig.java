package com.kilimo.ticket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
   
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    private JwtFilter jwtFilter;
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @Autowired
    private OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    @Autowired
    private Environment environment;

    @Bean
    public SecurityFilterChain filterchain(HttpSecurity http) throws Exception{
        http 
             .csrf(customizer-> customizer.disable())
             .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
             .httpBasic(basic -> basic.disable())
             .authorizeHttpRequests(request-> request
                .requestMatchers("/", "/signup", "/login", "/forgot-password", "/reset-password").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/users/avatar/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/departments/**", "/ticket-categories/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/auth/sign").permitAll()
                .requestMatchers("/knowledge/public/**").permitAll()

                //
                .requestMatchers("/dashboard").hasAnyRole("ADMIN", "USER", "CLIENT", "STAFF")
                .requestMatchers("/dashboard/admin").hasRole("ADMIN")

            // Admin only
                .requestMatchers("/admin/**").hasRole("ADMIN")

            // ICT Staff
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

            // Clients
                .requestMatchers("/client/**").hasAnyRole("USER", "CLIENT")

            // Tickets
                .requestMatchers("/tickets/create").hasAnyRole("USER", "CLIENT", "ADMIN")
                .requestMatchers("/tickets/assign/**").hasAnyRole("ADMIN","STAFF")
                .requestMatchers("/tickets/**").authenticated()

            // Knowledge base
                .requestMatchers("/knowledge/create").hasAnyRole("ADMIN","STAFF")
                .requestMatchers("/knowledge/approve/**").hasRole("ADMIN")
                .requestMatchers("/knowledge/*/review").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/knowledge/**").hasRole("ADMIN")

            // Assets
                .requestMatchers("/assets/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/ratings/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/ratings/staff/*/details").hasAnyRole("ADMIN", "STAFF")

            // Departments
                .requestMatchers(HttpMethod.POST, "/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/departments/**").hasRole("ADMIN")

            // Audit logs
                .requestMatchers("/audit/**").hasRole("ADMIN")
                .anyRequest().authenticated())
             .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
             .logout(logout -> logout.permitAll());

        String googleClientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        if (googleClientId != null && !googleClientId.isBlank()) {
            http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler));
        }

        return http.build();
    }

    // @Bean
    // public UserDetailsService userDetails(){
    //      UserDetails user1=User.
    //                             withDefaultPasswordEncoder()
    //                             .username("papa")
    //                             .password("1234")
    //                             .build();
    //     return new InMemoryUserDetailsManager(user1);                                
    // }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(10));
        
        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Keep configured VPS/public origins and always include local frontend origins for dev testing.
        String allowedOriginsEnv = environment.getProperty("CORS_ALLOWED_ORIGINS", "");
        LinkedHashSet<String> allowedOrigins = new LinkedHashSet<>(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
        ));

        if (allowedOriginsEnv != null && !allowedOriginsEnv.isBlank()) {
            Arrays.stream(allowedOriginsEnv.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .forEach(allowedOrigins::add);
        }
        
        configuration.setAllowedOrigins(new ArrayList<>(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
