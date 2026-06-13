package com.linkmesh.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

public class CustomAuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver delegate;

    public CustomAuthorizationRequestResolver(
            ClientRegistrationRepository repository) {

        this.delegate =
                new DefaultOAuth2AuthorizationRequestResolver(
                        repository,
                        "/oauth2/authorization"
                );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request) {

        return customize(delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request,
            String clientRegistrationId) {

        return customize(
                delegate.resolve(
                        request,
                        clientRegistrationId
                )
        );
    }

    private OAuth2AuthorizationRequest customize(
            OAuth2AuthorizationRequest request) {

        if (request == null) {
            return null;
        }

        Map<String, Object> params =
                new HashMap<>(request.getAdditionalParameters());

        params.put("prompt", "select_account");

        return OAuth2AuthorizationRequest
                .from(request)
                .additionalParameters(params)
                .build();
    }
}