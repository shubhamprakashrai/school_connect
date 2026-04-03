package com.schoolmgmt.scheduler;

import com.schoolmgmt.model.Tenant;
import com.schoolmgmt.repository.TenantRepository;
import com.schoolmgmt.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final BillingService billingService;
    private final TenantRepository tenantRepository;

    @Scheduled(cron = "${billing.student-snapshot.cron}")
    public void dailyStudentSnapshot() {
        log.info("Starting daily student snapshot...");
        List<String> activeTenantIds = tenantRepository.findByStatus(Tenant.TenantStatus.ACTIVE)
                .stream()
                .map(Tenant::getIdentifier)
                .collect(Collectors.toList());

        billingService.snapshotStudents(activeTenantIds);
        log.info("Daily student snapshot complete for {} tenants", activeTenantIds.size());
    }

    @Scheduled(cron = "${billing.invoice-generation.cron}")
    public void dailyInvoiceGeneration() {
        log.info("Starting invoice generation...");
        billingService.generateInvoices();
        log.info("Invoice generation complete");
    }

    @Scheduled(cron = "${billing.grace-period-check.cron}")
    public void dailyGracePeriodCheck() {
        log.info("Starting grace period enforcement...");
        billingService.enforceGracePeriods();
        log.info("Grace period enforcement complete");
    }
}
