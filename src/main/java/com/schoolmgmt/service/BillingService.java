package com.schoolmgmt.service;

import com.razorpay.RazorpayException;
import com.schoolmgmt.model.*;
import com.schoolmgmt.model.SchoolSubscription.SubscriptionStatus;
import com.schoolmgmt.model.SubscriptionInvoice.InvoiceStatus;
import com.schoolmgmt.model.SubscriptionPlan.BillingCycle;
import com.schoolmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final SchoolSubscriptionRepository subscriptionRepository;
    private final SubscriptionInvoiceRepository invoiceRepository;
    private final BillableStudentLogRepository studentLogRepository;
    private final StudentRepository studentRepository;
    private final RazorpayService razorpayService;

    @Value("${billing.default.invoice-due-days:7}")
    private int invoiceDueDays;

    private static final AtomicLong invoiceCounter = new AtomicLong(1);

    @Transactional
    public void snapshotStudents(List<String> activeTenantIds) {
        LocalDate today = LocalDate.now();
        for (String tenantId : activeTenantIds) {
            try {
                List<UUID> studentIds = studentRepository.findActiveStudentIdsByTenantId(tenantId);
                for (UUID studentId : studentIds) {
                    if (!studentLogRepository.existsByTenantIdAndStudentIdAndSnapshotDate(tenantId, studentId, today)) {
                        BillableStudentLog logEntry = BillableStudentLog.builder()
                                .tenantId(tenantId)
                                .studentId(studentId)
                                .snapshotDate(today)
                                .isActive(true)
                                .build();
                        studentLogRepository.save(logEntry);
                    }
                }
                log.debug("Snapshotted {} students for tenant {}", studentIds.size(), tenantId);
            } catch (Exception e) {
                log.error("Failed to snapshot students for tenant {}: {}", tenantId, e.getMessage());
            }
        }
    }

    @Transactional
    public void generateInvoices() {
        LocalDate today = LocalDate.now();
        List<SchoolSubscription> dueSubscriptions = subscriptionRepository
                .findByNextBillingDateAndAutoRenewTrue(today);

        for (SchoolSubscription subscription : dueSubscriptions) {
            try {
                generateInvoiceForSubscription(subscription);
            } catch (Exception e) {
                log.error("Failed to generate invoice for tenant {}: {}",
                        subscription.getTenantId(), e.getMessage());
            }
        }
        log.info("Invoice generation complete. Processed {} subscriptions", dueSubscriptions.size());
    }

    @Transactional
    public SubscriptionInvoice generateInvoiceForSubscription(SchoolSubscription subscription) {
        SubscriptionPlan plan = subscription.getPlan();
        LocalDate periodStart = subscription.getCurrentCycleStart();
        LocalDate periodEnd = subscription.getCurrentCycleEnd();

        int billableCount = countBillableStudents(
                subscription.getTenantId(), periodStart, periodEnd, plan.getMinDaysForBilling());

        BigDecimal studentCharge = plan.getPerStudentPrice().multiply(BigDecimal.valueOf(billableCount));
        BigDecimal totalAmount = plan.getBasePrice().add(studentCharge);

        String invoiceNumber = generateInvoiceNumber();

        SubscriptionInvoice invoice = SubscriptionInvoice.builder()
                .tenantId(subscription.getTenantId())
                .subscription(subscription)
                .invoiceNumber(invoiceNumber)
                .billingPeriodStart(periodStart)
                .billingPeriodEnd(periodEnd)
                .baseAmount(plan.getBasePrice())
                .perStudentAmount(plan.getPerStudentPrice())
                .billableStudentCount(billableCount)
                .totalAmount(totalAmount)
                .dueDate(LocalDate.now().plusDays(invoiceDueDays))
                .status(InvoiceStatus.PENDING)
                .build();

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoice.setPaymentMode(SubscriptionInvoice.PaymentMode.MANUAL);
            invoice.setManualPaymentNote("Zero amount - auto-marked as paid");
        } else {
            try {
                JSONObject notes = new JSONObject();
                notes.put("tenantId", subscription.getTenantId());
                notes.put("invoiceNumber", invoiceNumber);
                String orderId = razorpayService.createOrder(totalAmount, invoiceNumber, notes);
                invoice.setRazorpayOrderId(orderId);
            } catch (RazorpayException e) {
                log.error("Failed to create Razorpay order for invoice {}: {}",
                        invoiceNumber, e.getMessage());
            }
        }

        invoice = invoiceRepository.save(invoice);
        advanceBillingCycle(subscription);

        log.info("Generated invoice {} for tenant {} — {} students, total ₹{}",
                invoiceNumber, subscription.getTenantId(), billableCount, totalAmount);
        return invoice;
    }

    @Transactional
    public SubscriptionInvoice createRechargeInvoice(SchoolSubscription subscription, int months) {
        SubscriptionPlan plan = subscription.getPlan();

        int currentStudentCount = (int) studentRepository
                .countByTenantIdAndIsDeletedFalse(subscription.getTenantId());

        int numberOfCycles = calculateCyclesForMonths(plan.getBillingCycle(), months);
        BigDecimal cycleAmount = plan.getBasePrice()
                .add(plan.getPerStudentPrice().multiply(BigDecimal.valueOf(currentStudentCount)));
        BigDecimal totalAmount = cycleAmount.multiply(BigDecimal.valueOf(numberOfCycles));

        String invoiceNumber = generateInvoiceNumber();
        LocalDate rechargeStart = subscription.getExpiresAt() != null
                ? subscription.getExpiresAt().plusDays(1)
                : LocalDate.now();
        LocalDate rechargeEnd = rechargeStart.plusMonths(months);

        SubscriptionInvoice invoice = SubscriptionInvoice.builder()
                .tenantId(subscription.getTenantId())
                .subscription(subscription)
                .invoiceNumber(invoiceNumber)
                .billingPeriodStart(rechargeStart)
                .billingPeriodEnd(rechargeEnd)
                .baseAmount(plan.getBasePrice())
                .perStudentAmount(plan.getPerStudentPrice())
                .billableStudentCount(currentStudentCount)
                .totalAmount(totalAmount)
                .dueDate(LocalDate.now().plusDays(invoiceDueDays))
                .status(InvoiceStatus.PENDING)
                .build();

        try {
            JSONObject notes = new JSONObject();
            notes.put("tenantId", subscription.getTenantId());
            notes.put("type", "recharge");
            notes.put("months", months);
            String orderId = razorpayService.createOrder(totalAmount, invoiceNumber, notes);
            invoice.setRazorpayOrderId(orderId);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for recharge: {}", e.getMessage());
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Created recharge invoice {} for tenant {} — {} months, ₹{}",
                invoiceNumber, subscription.getTenantId(), months, totalAmount);
        return invoice;
    }

    @Transactional
    public void handlePaymentSuccess(SubscriptionInvoice invoice, String razorpayPaymentId,
                                      SubscriptionInvoice.PaymentMode paymentMode) {
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setRazorpayPaymentId(razorpayPaymentId);
        invoice.setPaymentMode(paymentMode);
        invoiceRepository.save(invoice);

        SchoolSubscription subscription = invoice.getSubscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setExpiresAt(invoice.getBillingPeriodEnd());
        subscriptionRepository.save(subscription);

        log.info("Payment successful for invoice {} — subscription {} activated until {}",
                invoice.getInvoiceNumber(), subscription.getTenantId(), invoice.getBillingPeriodEnd());
    }

    @Transactional
    public void enforceGracePeriods() {
        LocalDate today = LocalDate.now();
        List<SchoolSubscription> expired = subscriptionRepository.findExpiredSubscriptions(today);

        for (SchoolSubscription subscription : expired) {
            try {
                SubscriptionPlan plan = subscription.getPlan();
                long daysExpired = java.time.temporal.ChronoUnit.DAYS.between(
                        subscription.getExpiresAt(), today);

                SubscriptionStatus newStatus;
                if (daysExpired <= plan.getGracePeriodDays()) {
                    newStatus = SubscriptionStatus.GRACE;
                } else if (daysExpired <= plan.getGracePeriodDays() + plan.getReadOnlyPeriodDays()) {
                    newStatus = SubscriptionStatus.READ_ONLY;
                } else {
                    newStatus = SubscriptionStatus.SUSPENDED;
                }

                if (subscription.getStatus() != newStatus) {
                    subscription.setStatus(newStatus);
                    subscriptionRepository.save(subscription);
                    log.info("Tenant {} subscription status changed to {} (expired {} days ago)",
                            subscription.getTenantId(), newStatus, daysExpired);
                }
            } catch (Exception e) {
                log.error("Grace period check failed for tenant {}: {}",
                        subscription.getTenantId(), e.getMessage());
            }
        }
    }

    private int countBillableStudents(String tenantId, LocalDate start, LocalDate end, int minDays) {
        Long count = studentLogRepository.countBillableStudents(tenantId, start, end, minDays);
        return count != null ? count.intValue() : 0;
    }

    private void advanceBillingCycle(SchoolSubscription subscription) {
        SubscriptionPlan plan = subscription.getPlan();
        LocalDate nextStart = subscription.getCurrentCycleEnd().plusDays(1);
        LocalDate nextEnd = calculateCycleEnd(nextStart, plan.getBillingCycle());

        subscription.setCurrentCycleStart(nextStart);
        subscription.setCurrentCycleEnd(nextEnd);
        subscription.setNextBillingDate(nextEnd);
        subscriptionRepository.save(subscription);
    }

    private LocalDate calculateCycleEnd(LocalDate start, BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> start.plusMonths(1).minusDays(1);
            case QUARTERLY -> start.plusMonths(3).minusDays(1);
            case HALF_YEARLY -> start.plusMonths(6).minusDays(1);
            case YEARLY -> start.plusYears(1).minusDays(1);
        };
    }

    private int calculateCyclesForMonths(BillingCycle cycle, int months) {
        return switch (cycle) {
            case MONTHLY -> months;
            case QUARTERLY -> (int) Math.ceil(months / 3.0);
            case HALF_YEARLY -> (int) Math.ceil(months / 6.0);
            case YEARLY -> (int) Math.ceil(months / 12.0);
        };
    }

    private String generateInvoiceNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long counter = invoiceCounter.getAndIncrement();
        return String.format("INV-%s-%04d", datePrefix, counter);
    }
}
