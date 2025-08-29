package com.schoolmgmt.Repository;

import com.schoolmgmt.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void testNextTenantSequence() {
        System.out.println(tenantRepository.getNextTenantSequence());
    }

    @Test
    void testExistsMethods() {
        System.out.println(tenantRepository.existsBySubdomain("test"));
    }
}
