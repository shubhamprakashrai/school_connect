package com.schoolmgmt.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.json:}")
    private String firebaseCredentialsJson;

    @Value("${firebase.credentials.file:firebase-service-account.json}")
    private String firebaseCredentialsFile;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options;

            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
                // Use JSON string from env variable (for Render/cloud deployment)
                log.info("Initializing Firebase from FIREBASE_CREDENTIALS_JSON env variable");
                InputStream stream = new ByteArrayInputStream(
                        firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(stream))
                        .build();
            } else {
                // Use file from classpath (for local development)
                Resource resource = new ClassPathResource(firebaseCredentialsFile);
                if (resource.exists()) {
                    log.info("Initializing Firebase from classpath: {}", firebaseCredentialsFile);
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                            .build();
                } else {
                    log.warn("Firebase credentials not found. Push notifications will be disabled. " +
                            "Set FIREBASE_CREDENTIALS_JSON env var or place {} in resources.",
                            firebaseCredentialsFile);
                    return null;
                }
            }

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("FirebaseMessaging not available - FirebaseApp is null");
            return null;
        }
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
