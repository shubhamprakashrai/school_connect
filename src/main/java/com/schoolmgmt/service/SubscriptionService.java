package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.AssignSubscriptionRequest;
import com.schoolmgmt.model.SchoolSubscription;
import com.schoolmgmt.model.SchoolSubscription.SubscriptionStatus;
import com.schoolmgmt.model.SubscriptionInvoice;
import com.schoolmgmt.model.SubscriptionPlan;
import com.schoolmgmt.repository.SchoolSubscriptionRepository;
import com.schoolmgmt.repository.SubscriptionInvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final SubscriptionPlanService planService;

    @Transactional
    public SchoolSubscription assignPlan(AssignSubscriptionRequest request) {
        subscriptionRepository.findByTenantId(request.getTenantId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Subscription already exists for tenant: " + request.getTenantId());
                });

        SubscriptionPlan plan = planService.getPlanById(request.getPlanId());
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate cycleEnd = calculateCycleEnd(startDate, plan.getBillingCycle());

        SchoolSubscription subscription = SchoolSubscription.builder()
                .tenantId(request.getTenantId())
                .plan(plan)
                .currentCycleStart(startDate)
                .currentCycleEnd(cycleEnd)
                .nextBillingDate(cycleEnd)
                .expiresAt(cycleEnd)
                .status(SubscriptionStatus.ACTIVE)
                .autoRenew(request.getAutoRenew())
                .build();

        log.info("Assigned plan {} to tenant {}", plan.getName(), request.getTenantId());
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public SchoolSubscription getByTenantId(String tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NoSuchElementException("No subscription found for tenant: " + tenantId));
    }

    @Transactional(readOnly = true)
    public List<SchoolSubscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Transactional
    public SchoolSubscription overrideStatus(UUID subscriptionId, SubscriptionStatus status) {
        SchoolSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NoSuchElementException("Subscription not found: " + subscriptionId));
        subscription.setStatus(status);
        log.info("Override subscription status for tenant {} to {}", subscription.getTenantId(), status);
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionInvoice> getInvoicesByTenant(String tenantId) {
        return invoiceRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionInvoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public SubscriptionInvoice getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));
    }

    private LocalDate calculateCycleEnd(LocalDate start, SubscriptionPlan.BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> start.plusMonths(1).minusDays(1);
            case QUARTERLY -> start.plusMonths(3).minusDays(1);
            case HALF_YEARLY -> start.plusMonths(6).minusDays(1);
            case YEARLY -> start.plusYears(1).minusDays(1);
        };
    }
}
