package com.saboresperu.restaurante.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final RoleBasedAuthSuccessHandler successHandler;
    private final SecurityVersionFilter securityVersionFilter;
    private final AuthenticationFailureHandler loginFailureHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/menu",
                                "/menu/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/legal/**",
                                "/media/**",
                                "/webjars/**",
                                "/error",
                                "/error/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/login", "/register", "/register/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMINISTRATOR", "MANAGER", "OPERATIONS", "WAREHOUSE", "CUSTOMER_SERVICE", "CASHIER")
                        .requestMatchers("/kitchen/**").hasAnyRole("CHEF", "ADMINISTRATOR", "MANAGER")
                        .requestMatchers("/courier/**").hasAnyRole("COURIER", "ADMINISTRATOR", "MANAGER")
                        .requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMINISTRATOR")
                        .requestMatchers("/api/geo/**").authenticated()
                        .requestMatchers("/api/cart/**").hasAnyRole("CUSTOMER", "ADMINISTRATOR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")
                                    || request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(403);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"ok\":false,\"message\":\"Acceso denegado\"}");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/error/403");
                            }
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().contains("/api/")) {
                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"ok\":false,\"message\":\"No autenticado\"}");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        })
                )
                .csrf(csrf -> {
                    CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
                    repo.setCookiePath("/");
                    csrf.csrfTokenRepository(repo);
                    csrf.csrfTokenRequestHandler(new org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler());
                })
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        http.addFilterAfter(securityVersionFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}