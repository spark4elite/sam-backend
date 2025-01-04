package com.app.sam_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")  // Secure Swagger UI
                .authenticated()
                // Require authentication for POST /shorten
                .requestMatchers(HttpMethod.POST, "/shorten").authenticated()
                // Require authentication for GET /all
                .requestMatchers(HttpMethod.GET, "/all").authenticated()
                // Require authentication for GET /searchByName
                .requestMatchers(HttpMethod.GET, "/searchByName").authenticated()
                // Require authentication for GET /searchByLocation
                .requestMatchers(HttpMethod.GET, "/searchByLocation").authenticated()
                // Allow public access to GET /{shortenedUrl}
                .requestMatchers(HttpMethod.GET, "/{shortenedUrl}").permitAll()
                // Any other request requires authentication
                .anyRequest().authenticated()
                .and()
                .formLogin()  // Enable form login
                .permitAll()
                .and()
                .httpBasic()  // Allow basic authentication
                .and()
                .csrf().disable();  // Disable CSRF for Swagger UI (it's stateless)

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        User.UserBuilder users = User.builder();
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(users.username("admin").password(passwordEncoder().encode("admin123")).roles("ADMIN").build());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Use BCrypt for encoding passwords
    }
}


