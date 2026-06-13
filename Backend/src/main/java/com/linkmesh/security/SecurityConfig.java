package com.linkmesh.security;

import com.linkmesh.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler successHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/v1/url/health",
                                "/actuator/**",
                                "/logout"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/v1/url/*").permitAll()

                        .requestMatchers(HttpMethod.POST, "/v1/url").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/v1/url/*").authenticated()

                        .requestMatchers(
                                "/v1/url/my-urls",
                                "/me",
                                "/me/upgrade",
                                "/me/downgrade"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(
                                        new CustomAuthorizationRequestResolver(clientRegistrationRepository)
                                )
                        )
                        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("http://localhost:5173")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}