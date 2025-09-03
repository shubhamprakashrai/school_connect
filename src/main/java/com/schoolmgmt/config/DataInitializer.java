package com.schoolmgmt.config;

import com.schoolmgmt.model.User;
import com.schoolmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Data initializer to create default system users
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createSuperAdminIfNotExists();
    }

    private void createSuperAdminIfNotExists() {
        if (!userRepository.existsByEmailAndTenantId("superadmin@system.com", "SYSTEM")) {
            User superAdmin = User.builder()
                    .userId("SUPER_ADMIN_001")
                    .email("superadmin@system.com")
                    .username("superadmin@system.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phone("+1000000000")
                    .role(User.UserRole.SUPER_ADMIN)
                    .status(User.UserStatus.ACTIVE)
                    .emailVerified(true)
                    .build();
            
            superAdmin.setTenantId("SYSTEM");
            superAdmin.setCreatedBy("SYSTEM_INIT");
            superAdmin.setUpdatedBy("SYSTEM_INIT");

            userRepository.save(superAdmin);
            log.info("Super admin user created successfully: {}", superAdmin.getEmail());
        } else {
            log.info("Super admin user already exists");
        }
    }
}