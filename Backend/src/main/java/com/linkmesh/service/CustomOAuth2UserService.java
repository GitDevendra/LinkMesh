package com.linkmesh.service;

import com.linkmesh.entity.User;
import com.linkmesh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {

        OAuth2User oauthUser = super.loadUser(request);

        String googleId = oauthUser.getAttribute("sub");
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");

        userRepository.findByGoogleId(googleId)
                .orElseGet(() ->
                        userRepository.save(
                                User.builder()
                                        .googleId(googleId)
                                        .email(email)
                                        .name(name)
                                        .pictureUrl(picture)
                                        .build()
                        ));

        return oauthUser;
    }

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String googleId = oauthUser.getAttribute("sub");

        return userRepository.findByGoogleId(googleId)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }
}