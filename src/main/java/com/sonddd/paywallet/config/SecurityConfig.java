package com.sonddd.paywallet.config;

import com.sonddd.paywallet.security.CustomUserDetailsService;
import com.sonddd.paywallet.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật: stateless (JWT), phân quyền theo URL, BCrypt cho mật khẩu.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // cho phép dùng @PreAuthorize ở tầng service/controller
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                       // API stateless không dùng CSRF token
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Tài nguyên công khai
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico",
                                "/css/**", "/js/**", "/assets/**",
                                "/api/auth/**",
                                "/api/merchants",            // xem danh sách merchant để demo
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/h2-console/**",
                                "/actuator/health"
                        ).permitAll()
                        // Khu vực quản trị
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )
                // Cho phép H2 console hiển thị trong iframe (chỉ phục vụ môi trường dev)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
