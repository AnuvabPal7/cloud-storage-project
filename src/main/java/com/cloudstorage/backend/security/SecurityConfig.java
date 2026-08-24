package com.cloudstorage.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central security rulebook for the app. Key decisions explained inline
 * below, since these are the ones interviewers actually ask about.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    // BCrypt: a one-way hash designed to be *slow* on purpose, which makes
    // brute-forcing stolen password hashes impractical. Never store raw
    // passwords or use fast hashes like MD5/SHA-256 for this.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Spring Security's newer API: UserDetailsService now goes into the
        // constructor instead of a setUserDetailsService() setter (that
        // setter was removed). setPasswordEncoder() is still a setter.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection matters for cookie-based sessions (browsers auto-send
                // cookies). We're stateless + JWT-in-header, which isn't vulnerable to
                // CSRF the same way, so it's safe - and necessary - to disable here.
                .csrf(csrf -> csrf.disable())

                // No HttpSession. Every request must prove who it is via its own JWT.
                // This is what makes JWT auth horizontally scalable - any server
                // instance can validate any request without shared session state.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // register/login must be reachable without a token
                        .anyRequest().authenticated()                 // everything else requires a valid JWT
                )

                .authenticationProvider(authenticationProvider())

                // Insert our filter BEFORE Spring's default username/password filter,
                // so JWT auth runs first on every request.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}