package com.nbhang.utils;

import com.nbhang.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
        @Bean
        public UserDetailsService userDetailsService(UserService userService) {
                return userService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
                DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService);
                auth.setPasswordEncoder(passwordEncoder());
                return auth;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
                return http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/css/**", "/js/**", "/",
                                                "/register", "/error")
                                .permitAll()
                                .requestMatchers("/books/edit",
                                                "/books/delete")
                                .authenticated()
                                .requestMatchers("/books", "/books/add")
                                .authenticated()
                                .requestMatchers("/api/**")
                                .authenticated()
                                .anyRequest().authenticated())
                                .logout(logout -> logout.logoutUrl("/logout")
                                                .logoutSuccessUrl("/login")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .formLogin(formLogin -> formLogin.loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/login?error=true")
                                                .permitAll())
                                .rememberMe(rememberMe -> rememberMe.key("hutech")
                                                .rememberMeCookieName("hutech")
                                                .tokenValiditySeconds(24 * 60 * 60)
                                                .userDetailsService(userDetailsService))
                                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedPage("/403"))
                                .sessionManagement(sessionManagement -> sessionManagement.maximumSessions(1)
                                                .expiredUrl("/login"))
                                .httpBasic(httpBasic -> httpBasic.realmName("hutech"))
                                .build();
        }
}