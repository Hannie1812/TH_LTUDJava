package com.nbhang.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbhang.entities.User;
import com.nbhang.entities.UserCredential;
import com.nbhang.repositories.UserCredentialRepository;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.util.Base64UrlUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebAuthnService {

    private final UserCredentialRepository userCredentialRepository;
    private final WebAuthnManager webAuthnManager;
    private final ObjectMapper objectMapper;

    // Use localhost for dev, should be configurable for prod
    private final Origin origin = new Origin("http://localhost:8080");
    private final String rpId = "localhost";

    public String generateChallenge() {
        return Base64UrlUtil.encodeToString(new DefaultChallenge().getValue());
    }

    @Transactional
    public void registerCredential(User user, String credentialId, String userHandle, String publicKey,
            long signCount) {
        UserCredential credential = UserCredential.builder()
                .credentialId(credentialId)
                .userHandle(userHandle)
                .publicKey(publicKey)
                .signCount(signCount)
                .user(user)
                .build();
        userCredentialRepository.save(credential);
    }

    public List<UserCredential> getCredentials(User user) {
        return userCredentialRepository.findByUser(user);
    }

    public UserCredential getCredential(String credentialId) {
        return userCredentialRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
    }

    @Transactional
    public void updateSignCount(UserCredential credential, long newSignCount) {
        credential.setSignCount(newSignCount);
        userCredentialRepository.save(credential);
    }
}
