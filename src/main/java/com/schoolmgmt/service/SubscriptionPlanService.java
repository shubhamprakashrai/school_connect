package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.CreateSubscriptionPlanRequest;
import com.schoolmgmt.model.SubscriptionPlan;
import com.schoolmgmt.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    @Transactional
    public SubscriptionPlan createPlan(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.getName())
                .tenantId(request.getTenantId())
                .basePrice(request.getBasePrice())
                .perStudentPrice(request.getPerStudentPrice())
                .billingCycle(request.getBillingCycle())
                .gracePeriodDays(request.getGracePeriodDays())
                .readOnlyPeriodDays(request.getReadOnlyPeriodDays())
                .minDaysForBilling(request.getMinDaysForBilling())
                .build();

        log.info("Creating subscription plan: {} basePrice={} perStudent={}",
                plan.getName(), plan.getBasePrice(), plan.getPerStudentPrice());
        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllActivePlans() {
        return planRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanById(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Plan not found: " + id));
    }

    @Transactional
    public SubscriptionPlan updatePlan(UUID id, CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = getPlanById(id);
        if (request.getName() != null) plan.setName(request.getName());
        if (request.getBasePrice() != null) plan.setBasePrice(request.getBasePrice());
        if (request.getPerStudentPrice() != null) plan.setPerStudentPrice(request.getPerStudentPrice());
        if (request.getBillingCycle() != null) plan.setBillingCycle(request.getBillingCycle());
        if (request.getGracePeriodDays() != null) plan.setGracePeriodDays(request.getGracePeriodDays());
        if (request.getReadOnlyPeriodDays() != null) plan.setReadOnlyPeriodDays(request.getReadOnlyPeriodDays());
        if (request.getMinDaysForBilling() != null) plan.setMinDaysForBilling(request.getMinDaysForBilling());
        log.info("Updated subscription plan: {}", plan.getName());
        return planRepository.save(plan);
    }

    @Transactional
    public void deactivatePlan(UUID id) {
        SubscriptionPlan plan = getPlanById(id);
        plan.setIsActive(false);
        planRepository.save(plan);
        log.info("Deactivated subscription plan: {}", plan.getName());
    }
}
