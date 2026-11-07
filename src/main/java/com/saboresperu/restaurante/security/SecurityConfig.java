package com.saboresperu.restaurante.security;

import com.saboresperu.restaurante.security.CustomUserDetailsService;
import com.saboresperu.restaurante.security.RoleBasedAuthSuccessHandler;
import com.saboresperu.restaurante.security.SecurityVersionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final LogoutAuditHandler logoutSuccessHandler;

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

                        /*.requestMatchers("/admin/users/**", "/admin/roles/**", "/admin/permission/**")
                        .hasAnyAuthority("PERM_USERS_VIEW", "PERM_ROLES_MANAGE", "PERM_PERMISSIONS_MANAGE")
                        .requestMatchers("/admin/audit/**")
                        .hasAnyAuthority("PERM_ACCESS_LOGS_VIEW", "PERM_SECURITY_AUDIT_VIEW")*/
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
                        .accessDeniedPage("/error/403")
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                );

        http.addFilterAfter(securityVersionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}