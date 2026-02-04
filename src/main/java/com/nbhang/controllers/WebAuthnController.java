package com.nbhang.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbhang.entities.User;
import com.nbhang.services.UserService;
import com.nbhang.services.WebAuthnService;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.util.Base64UrlUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webauthn")
@RequiredArgsConstructor
@Slf4j
public class WebAuthnController {

    private final WebAuthnService webAuthnService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping("/register/start")
    public ResponseEntity<String> startRegistration(HttpSession session) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate challenge
        String challenge = webAuthnService.generateChallenge();
        session.setAttribute("challenge", challenge);

        // In a real implementation, you would generate full
        // PublicKeyCredentialCreationOptions here
        // For simplicity in this demo, we return basic info and let JS handle some
        // parts or use a default
        Map<String, Object> options = new HashMap<>();
        options.put("challenge", challenge);
        options.put("userId", Base64UrlUtil.encodeToString(user.getId().toString().getBytes()));
        options.put("username", user.getUsername());
        options.put("displayName", user.getEmail());

        try {
            return ResponseEntity.ok(objectMapper.writeValueAsString(options));
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/register/finish")
    public ResponseEntity<String> finishRegistration(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String credentialId = body.get("credentialId");
            String clientDataJSON = body.get("clientDataJSON");
            String attestationObject = body.get("attestationObject");

            // In a real app, verify these against the challenge in session
            // webAuthnManager.validate(...)

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByUsername(username).orElseThrow();

            // Saving 'fake' public key for demo purposes as full validation requires more
            // code
            // You would normally parse the attestationObject to get the public key
            webAuthnService.registerCredential(user, credentialId, user.getUsername(), "publicKeyPlaceholder", 0);

            return ResponseEntity.ok("Registration successful");
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.badRequest().body("Registration failed");
        }
    }

    @PostMapping("/login/start")
    public ResponseEntity<String> startLogin(HttpSession session) {
        String challenge = webAuthnService.generateChallenge();
        session.setAttribute("challenge", challenge);

        Map<String, Object> options = new HashMap<>();
        options.put("challenge", challenge);

        try {
            return ResponseEntity.ok(objectMapper.writeValueAsString(options));
        } catch (JsonProcessingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/login/finish")
    public ResponseEntity<String> finishLogin(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String credentialId = body.get("credentialId");
            // Verify challenge...

            // Find user by credential ID
            var credential = webAuthnService.getCredential(credentialId);
            User user = credential.getUser();

            // Manually log the user in
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                    user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return ResponseEntity.ok("Login successful");
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body("Login failed");
        }
    }
}
