package com.nbhang.utils;

import com.nbhang.services.UserService;
import com.nbhang.services.OAuthService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
        private final OAuthService oAuthService;

        @Autowired
        private UserService userService;

        @Bean
        public UserDetailsService userDetailsService() {
                return userService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());

                authProvider.setPasswordEncoder(passwordEncoder());

                return authProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/",
                                                                "/oauth/**", "/register", "/error",
                                                                "/test/**", "/images/**", "/webauthn/**")
                                                .permitAll()
                                                .requestMatchers("/books/edit/**",
                                                                "/books/add", "/books/delete")
                                                .hasAnyAuthority("ADMIN")
                                                .requestMatchers("/books", "/cart", "/cart/**")
                                                .hasAnyAuthority("ADMIN", "USER")
                                                .requestMatchers("/api/**")
                                                // .hasAnyAuthority("ADMIN", "USER")
                                                .permitAll()
                                                .requestMatchers("/profile", "/profile/**", "/orders", "/orders/**")
                                                .hasAnyAuthority("ADMIN", "USER")
                                                .requestMatchers("/admin/**")
                                                .hasAnyAuthority("ADMIN")
                                                .anyRequest().authenticated())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .oauth2Login(
                                                oauth2Login -> oauth2Login
                                                                .loginPage("/login")
                                                                .failureUrl("/login?error")
                                                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                                                                .userService(oAuthService))
                                                                .successHandler(
                                                                                (request, response,
                                                                                                authentication) -> {
                                                                                        try {
                                                                                                var oidcUser = (DefaultOidcUser) authentication
                                                                                                                .getPrincipal();
                                                                                                log.info("Google OAuth login attempt for email: {}",
                                                                                                                oidcUser.getEmail());
                                                                                                userService.saveOauthUser(
                                                                                                                oidcUser.getEmail(),
                                                                                                                oidcUser.getName());
                                                                                                // Load user by email để
                                                                                                // đảm bảo lấy đúng user
                                                                                                // vừa tạo
                                                                                                var userDetails = userService
                                                                                                                .loadUserByUsername(
                                                                                                                                oidcUser.getEmail());
                                                                                                log.info("Loaded user authorities: {}",
                                                                                                                userDetails.getAuthorities());
                                                                                                var authToken = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                                                                                userDetails,
                                                                                                                null,
                                                                                                                userDetails.getAuthorities());
                                                                                                org.springframework.security.core.context.SecurityContextHolder
                                                                                                                .getContext()
                                                                                                                .setAuthentication(
                                                                                                                                authToken);
                                                                                                response.sendRedirect(
                                                                                                                "/");
                                                                                        } catch (Exception e) {
                                                                                                log.error("Error during OAuth2 login",
                                                                                                                e);
                                                                                                response.sendRedirect(
                                                                                                                "/login?error=oauth2");
                                                                                        }
                                                                                })
                                                                .permitAll())
                                .rememberMe(rememberMe -> rememberMe
                                                .key("hutech")
                                                .rememberMeCookieName("hutech")
                                                .tokenValiditySeconds(24 * 60 * 60)
                                                .userDetailsService(userDetailsService()))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .accessDeniedPage("/403"))
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .maximumSessions(1)
                                                .expiredUrl("/login"))
                                .httpBasic(httpBasic -> httpBasic
                                                .realmName("hutech"))
                                .build();
        }
}