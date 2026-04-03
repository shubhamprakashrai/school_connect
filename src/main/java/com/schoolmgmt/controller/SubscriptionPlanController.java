package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.CreateSubscriptionPlanRequest;
import com.schoolmgmt.model.SubscriptionPlan;
import com.schoolmgmt.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/superadmin/subscription-plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    @PostMapping
    public ResponseEntity<SubscriptionPlan> createPlan(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> getPlan(@PathVariable UUID id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> updatePlan(@PathVariable UUID id,
                                                        @Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePlan(@PathVariable UUID id) {
        planService.deactivatePlan(id);
        return ResponseEntity.noContent().build();
    }
}
