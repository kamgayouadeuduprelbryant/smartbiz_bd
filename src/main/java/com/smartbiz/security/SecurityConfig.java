package com.smartbiz.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Toutes les autorisations sont verifiees ici cote serveur (pas seulement
 * masquees dans Thymeleaf) via hasRole/hasAuthority sur les routes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/css/**", "/js/**", "/images/**", "/icons/**",
                        "/auth/login", "/auth/register", "/auth/mot-de-passe-oublie",
                        "/error", "/webjars/**"
                ).permitAll()
                .requestMatchers("/parametres/profil", "/parametres/mot-de-passe").authenticated()
                .requestMatchers("/utilisateurs/**", "/parametres/**").hasRole("ADMIN")
                .requestMatchers("/rapports/**").hasAnyRole("ADMIN", "MANAGER", "COMPTABLE")
                .requestMatchers("/finances/**").hasAnyRole("ADMIN", "COMPTABLE", "MANAGER")
                .requestMatchers("/employes/**", "/departements/**", "/conges/**", "/absences/**", "/presences/**")
                    .hasAnyRole("ADMIN", "RH", "MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/auth/login?erreur")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .defaultSuccessUrl("/dashboard", true)
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?deconnexion")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .userDetailsService(customUserDetailsService)
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"))
            // CSRF reste actif partout (comportement par defaut de Spring Security).
            ;

        return http.build();
    }
}
