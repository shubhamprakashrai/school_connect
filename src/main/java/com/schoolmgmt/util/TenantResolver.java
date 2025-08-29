    package com.schoolmgmt.util;

    import com.schoolmgmt.model.Tenant;
    import com.schoolmgmt.repository.TenantRepository;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.cache.annotation.Cacheable;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.Optional;

    /**
     * Service to resolve and validate tenants.
     * Provides caching for performance optimization.
     */
    @Service
    @RequiredArgsConstructor
    @Slf4j
    @Transactional(readOnly = true)
    public class TenantResolver {

        private final TenantRepository tenantRepository;

        /**
         * Validate if a tenant exists and is active
         * @param tenantId The tenant identifier
         * @return true if tenant is valid and active, false otherwise
         */
        @Cacheable(value = "tenant-validation", key = "#tenantId")
        public boolean validateTenant(String tenantId) {
            Optional<Tenant> tenant = tenantRepository.findByIdentifier(tenantId);
            return tenant.map(Tenant::isActive).orElse(false);
        }

        /**
         * Get tenant ID by subdomain
         * @param subdomain The subdomain
         * @return The tenant identifier or null if not found
         */
        @Cacheable(value = "tenant-subdomain", key = "#subdomain")
        public String getTenantIdBySubdomain(String subdomain) {
            Optional<Tenant> tenant = tenantRepository.findBySubdomain(subdomain);
            return tenant.map(Tenant::getIdentifier).orElse(null);
        }

        /**
         * Get tenant by identifier
         * @param tenantId The tenant identifier
         * @return The tenant or null if not found
         */
        @Cacheable(value = "tenants", key = "#tenantId")
        public Tenant getTenant(String tenantId) {
            return tenantRepository.findByIdentifier(tenantId).orElse(null);
        }

        /**
         * Get tenant schema name
         * @param tenantId The tenant identifier
         * @return The schema name or null if not found
         */
        @Cacheable(value = "tenant-schema", key = "#tenantId")
        public String getTenantSchema(String tenantId) {
            Optional<Tenant> tenant = tenantRepository.findByIdentifier(tenantId);
            return tenant.map(Tenant::getSchemaName).orElse(null);
        }

        /**
         * Check if tenant has reached student limit
         * @param tenantId The tenant identifier
         * @return true if limit reached, false otherwise
         */
        public boolean hasReachedStudentLimit(String tenantId) {
            Tenant tenant = getTenant(tenantId);
            return tenant != null && !tenant.canAddStudent();
        }

        /**
         * Check if tenant has reached teacher limit
         * @param tenantId The tenant identifier
         * @return true if limit reached, false otherwise
         */
        public boolean hasReachedTeacherLimit(String tenantId) {
            Tenant tenant = getTenant(tenantId);
            return tenant != null && !tenant.canAddTeacher();
        }

        /**
         * Check if tenant has sufficient storage space
         * @param tenantId The tenant identifier
         * @param requiredMb Required storage in MB
         * @return true if has space, false otherwise
         */
        public boolean hasStorageSpace(String tenantId, int requiredMb) {
            Tenant tenant = getTenant(tenantId);
            return tenant != null && tenant.hasStorageSpace(requiredMb);
        }
    }
