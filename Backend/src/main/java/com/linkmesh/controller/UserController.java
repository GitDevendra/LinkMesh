package com.linkmesh.controller;

import com.linkmesh.entity.User;
import com.linkmesh.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.linkmesh.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OAuth2User oauthUser) {
        User user = customOAuth2UserService.getCurrentUser();

        Map<String, Object> response = new HashMap<>(oauthUser.getAttributes());
        response.put("role", user.getRole().name());   // "FREE" or "PREMIUM"
        return ResponseEntity.ok(response);
    }

    /** Simulates upgrading the current user to PREMIUM (for testing/demo). */
    @PostMapping("/me/upgrade")
    public ResponseEntity<Map<String, Object>> upgrade() {
        User user = customOAuth2UserService.getCurrentUser();
        user.setRole(User.Role.PREMIUM);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Upgraded to PREMIUM successfully",
                "role", user.getRole().name()
        ));
    }

    /** Downgrades back to FREE (useful for testing). */
    @PostMapping("/me/downgrade")
    public ResponseEntity<Map<String, Object>> downgrade() {
        User user = customOAuth2UserService.getCurrentUser();
        user.setRole(User.Role.FREE);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Downgraded to FREE",
                "role", user.getRole().name()
        ));
    }
}