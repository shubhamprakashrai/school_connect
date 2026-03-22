package com.schoolmgmt.service;

import com.schoolmgmt.dto.response.*;
import com.schoolmgmt.model.*;
import com.schoolmgmt.repository.*;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final AnnouncementRepository announcementRepository;
    private final SchoolClassRepository schoolClassRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        String tenantId = TenantContext.requireCurrentTenant();
        log.debug("Fetching dashboard stats for tenant: {}", tenantId);

        long totalStudents = studentRepository.countByTenantIdAndStatus(tenantId, Student.StudentStatus.ACTIVE)
                + studentRepository.countByTenantIdAndStatus(tenantId, Student.StudentStatus.INACTIVE);
        long activeStudents = studentRepository.countByTenantIdAndStatus(tenantId, Student.StudentStatus.ACTIVE);
        long totalTeachers = teacherRepository.countByTenantIdAndStatus(tenantId, Teacher.TeacherStatus.ACTIVE);
        long totalParents = parentRepository.countByTenantIdAndStatus(tenantId, Parent.ParentStatus.ACTIVE);

        // Today's attendance percentage
        double todayAttendancePercentage = calculateTodayAttendancePercentage(tenantId);

        // Fee amounts
        BigDecimal collectedFeeAmount = feePaymentRepository.getTotalCollectedByTenant(tenantId);
        BigDecimal pendingFeeAmount = feePaymentRepository.getTotalPendingByTenant(tenantId);

        // Pending leave requests
        List<LeaveRequest> pendingLeaves = leaveRequestRepository
                .findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, LeaveRequest.LeaveStatus.PENDING);
        long pendingLeaveRequests = pendingLeaves.size();

        // Upcoming exams
        List<Exam> upcomingExamList = examRepository.findUpcomingExams(tenantId, LocalDate.now());
        long upcomingExams = upcomingExamList.size();

        // Recent announcements (published and not expired)
        List<Announcement> recentAnnouncementList = announcementRepository
                .findByIsPublishedTrueAndExpiresAtAfterAndTenantIdOrderByPublishedAtDesc(
                        java.time.LocalDateTime.now(), tenantId);
        long recentAnnouncements = recentAnnouncementList.size();

        return DashboardStatsResponse.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalParents(totalParents)
                .activeStudents(activeStudents)
                .todayAttendancePercentage(todayAttendancePercentage)
                .pendingFeeAmount(pendingFeeAmount != null ? pendingFeeAmount : BigDecimal.ZERO)
                .collectedFeeAmount(collectedFeeAmount != null ? collectedFeeAmount : BigDecimal.ZERO)
                .pendingLeaveRequests(pendingLeaveRequests)
                .upcomingExams(upcomingExams)
                .recentAnnouncements(recentAnnouncements)
                .build();
    }

    @Transactional(readOnly = true)
    public AttendanceTrendResponse getAttendanceTrend(int days) {
        String tenantId = TenantContext.requireCurrentTenant();
        log.debug("Fetching attendance trend for last {} days, tenant: {}", days, tenantId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // Fetch all attendance records for the date range
        List<Attendance> allAttendance = attendanceRepository
                .findByStatusAndTenantId(Attendance.AttendanceStatus.PRESENT, tenantId);

        // We need all attendance records, so we query by date range using a specification approach
        // Since there's no direct method for all attendance by tenant + date range,
        // let's use available methods and group by date
        List<AttendanceTrendResponse.DailyAttendance> trend = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;

            // Use specification executor to count by date and tenant
            long presentCount = countAttendanceByStatusAndDate(tenantId, currentDate, Attendance.AttendanceStatus.PRESENT);
            long absentCount = countAttendanceByStatusAndDate(tenantId, currentDate, Attendance.AttendanceStatus.ABSENT);
            long total = presentCount + absentCount;
            double percentage = total > 0 ? (presentCount * 100.0 / total) : 0.0;
            percentage = Math.round(percentage * 100.0) / 100.0;

            trend.add(AttendanceTrendResponse.DailyAttendance.builder()
                    .date(currentDate)
                    .presentCount(presentCount)
                    .absentCount(absentCount)
                    .percentage(percentage)
                    .build());
        }

        return AttendanceTrendResponse.builder()
                .trend(trend)
                .build();
    }

    @Transactional(readOnly = true)
    public FeeAnalyticsResponse getFeeSummary() {
        String tenantId = TenantContext.requireCurrentTenant();
        log.debug("Fetching fee summary for tenant: {}", tenantId);

        BigDecimal totalCollected = feePaymentRepository.getTotalCollectedByTenant(tenantId);
        BigDecimal totalPending = feePaymentRepository.getTotalPendingByTenant(tenantId);

        // Total overdue
        List<FeePayment> overduePayments = feePaymentRepository
                .findByTenantIdAndPaymentStatus(tenantId, FeePayment.PaymentStatus.OVERDUE);
        BigDecimal totalOverdue = overduePayments.stream()
                .map(FeePayment::getBalanceAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly collection for last 6 months
        List<FeeAnalyticsResponse.MonthlyCollection> monthlyCollection = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();
            BigDecimal amount = feePaymentRepository.getCollectionBetweenDates(tenantId, monthStart, monthEnd);
            monthlyCollection.add(FeeAnalyticsResponse.MonthlyCollection.builder()
                    .month(ym.format(monthFormatter))
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .build());
        }

        // Payment mode breakdown
        List<FeePayment> paidPayments = feePaymentRepository
                .findByTenantIdAndPaymentStatus(tenantId, FeePayment.PaymentStatus.PAID);
        List<FeePayment> partialPayments = feePaymentRepository
                .findByTenantIdAndPaymentStatus(tenantId, FeePayment.PaymentStatus.PARTIAL);

        List<FeePayment> allPaidPayments = new ArrayList<>(paidPayments);
        allPaidPayments.addAll(partialPayments);

        Map<String, Long> paymentModeBreakdown = allPaidPayments.stream()
                .filter(fp -> fp.getPaymentMode() != null)
                .collect(Collectors.groupingBy(
                        fp -> fp.getPaymentMode().name().toLowerCase(),
                        Collectors.counting()));

        return FeeAnalyticsResponse.builder()
                .totalCollected(totalCollected != null ? totalCollected : BigDecimal.ZERO)
                .totalPending(totalPending != null ? totalPending : BigDecimal.ZERO)
                .totalOverdue(totalOverdue)
                .monthlyCollection(monthlyCollection)
                .paymentModeBreakdown(paymentModeBreakdown)
                .build();
    }

    @Transactional(readOnly = true)
    public StudentDemographicsResponse getStudentDemographics() {
        String tenantId = TenantContext.requireCurrentTenant();
        log.debug("Fetching student demographics for tenant: {}", tenantId);

        // Class-wise count using existing repository method
        List<Object[]> classStats = studentRepository.getStudentStatisticsByClass(tenantId);
        List<SchoolClass> allClasses = schoolClassRepository.findAllByTenantId(tenantId);
        Map<String, String> classIdToName = allClasses.stream()
                .collect(Collectors.toMap(
                        sc -> sc.getId().toString(),
                        SchoolClass::getName,
                        (a, b) -> a));

        List<StudentDemographicsResponse.ClassWiseCount> classWiseCount = new ArrayList<>();
        long totalMale = 0;
        long totalFemale = 0;
        long totalOther = 0;

        for (Object[] row : classStats) {
            String classId = row[0] != null ? row[0].toString() : "Unknown";
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long male = row[2] != null ? ((Number) row[2]).longValue() : 0;
            long female = row[3] != null ? ((Number) row[3]).longValue() : 0;

            totalMale += male;
            totalFemale += female;
            totalOther += (count - male - female);

            String className = classIdToName.getOrDefault(classId, classId);
            classWiseCount.add(StudentDemographicsResponse.ClassWiseCount.builder()
                    .className(className)
                    .count(count)
                    .build());
        }

        Map<String, Long> genderDistribution = new LinkedHashMap<>();
        genderDistribution.put("male", totalMale);
        genderDistribution.put("female", totalFemale);
        genderDistribution.put("other", totalOther);

        // New admissions this month
        LocalDate monthStart = YearMonth.now().atDay(1);
        LocalDate monthEnd = YearMonth.now().atEndOfMonth();
        List<Student> newAdmissions = studentRepository
                .findByAdmissionDateBetweenAndTenantId(monthStart, monthEnd, tenantId);
        long newAdmissionsThisMonth = newAdmissions.size();

        return StudentDemographicsResponse.builder()
                .classWiseCount(classWiseCount)
                .genderDistribution(genderDistribution)
                .newAdmissionsThisMonth(newAdmissionsThisMonth)
                .build();
    }

    @Transactional(readOnly = true)
    public ExamPerformanceResponse getExamPerformance() {
        String tenantId = TenantContext.requireCurrentTenant();
        log.debug("Fetching exam performance for tenant: {}", tenantId);

        // Get completed exams for this tenant
        List<Exam> completedExams = examRepository.findByTenantIdAndStatus(tenantId, Exam.ExamStatus.COMPLETED);

        if (completedExams.isEmpty()) {
            return ExamPerformanceResponse.builder()
                    .averageScore(0.0)
                    .highestScore(0.0)
                    .lowestScore(0.0)
                    .subjectWiseAverage(Collections.emptyList())
                    .gradeDistribution(Collections.emptyMap())
                    .build();
        }

        // Collect all results from completed exams
        List<ExamResult> allResults = new ArrayList<>();
        for (Exam exam : completedExams) {
            List<ExamResult> results = examResultRepository.findByExamId(exam.getId());
            allResults.addAll(results);
        }

        if (allResults.isEmpty()) {
            return ExamPerformanceResponse.builder()
                    .averageScore(0.0)
                    .highestScore(0.0)
                    .lowestScore(0.0)
                    .subjectWiseAverage(Collections.emptyList())
                    .gradeDistribution(Collections.emptyMap())
                    .build();
        }

        // Filter out absent students
        List<ExamResult> validResults = allResults.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsAbsent()) && r.getPercentage() != null)
                .collect(Collectors.toList());

        double averageScore = validResults.stream()
                .mapToDouble(ExamResult::getPercentage)
                .average()
                .orElse(0.0);
        averageScore = Math.round(averageScore * 100.0) / 100.0;

        double highestScore = validResults.stream()
                .mapToDouble(ExamResult::getPercentage)
                .max()
                .orElse(0.0);
        highestScore = Math.round(highestScore * 100.0) / 100.0;

        double lowestScore = validResults.stream()
                .mapToDouble(ExamResult::getPercentage)
                .min()
                .orElse(0.0);
        lowestScore = Math.round(lowestScore * 100.0) / 100.0;

        // Subject-wise average: group by exam's subjectName
        Map<String, List<ExamResult>> resultsBySubject = new HashMap<>();
        for (ExamResult result : validResults) {
            Exam exam = result.getExam();
            String subject = exam != null && exam.getSubjectName() != null ? exam.getSubjectName() : "Unknown";
            resultsBySubject.computeIfAbsent(subject, k -> new ArrayList<>()).add(result);
        }

        List<ExamPerformanceResponse.SubjectWiseAverage> subjectWiseAverage = resultsBySubject.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .mapToDouble(ExamResult::getPercentage)
                            .average()
                            .orElse(0.0);
                    return ExamPerformanceResponse.SubjectWiseAverage.builder()
                            .subject(entry.getKey())
                            .average(Math.round(avg * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(ExamPerformanceResponse.SubjectWiseAverage::getSubject))
                .collect(Collectors.toList());

        // Grade distribution
        Map<String, Long> gradeDistribution = new LinkedHashMap<>();
        gradeDistribution.put("A", 0L);
        gradeDistribution.put("B", 0L);
        gradeDistribution.put("C", 0L);
        gradeDistribution.put("D", 0L);
        gradeDistribution.put("F", 0L);

        for (ExamResult result : validResults) {
            String grade = result.getGrade();
            if (grade == null) continue;
            // Map detailed grades to broad categories: A+/A -> A, B+/B -> B, etc.
            String broadGrade;
            if (grade.startsWith("A")) {
                broadGrade = "A";
            } else if (grade.startsWith("B")) {
                broadGrade = "B";
            } else if (grade.equals("C")) {
                broadGrade = "C";
            } else if (grade.equals("D") || grade.equals("E")) {
                broadGrade = "D";
            } else {
                broadGrade = "F";
            }
            gradeDistribution.merge(broadGrade, 1L, Long::sum);
        }

        return ExamPerformanceResponse.builder()
                .averageScore(averageScore)
                .highestScore(highestScore)
                .lowestScore(lowestScore)
                .subjectWiseAverage(subjectWiseAverage)
                .gradeDistribution(gradeDistribution)
                .build();
    }

    // ---- Private helper methods ----

    private double calculateTodayAttendancePercentage(String tenantId) {
        LocalDate today = LocalDate.now();

        long presentCount = countAttendanceByStatusAndDate(tenantId, today, Attendance.AttendanceStatus.PRESENT);
        long absentCount = countAttendanceByStatusAndDate(tenantId, today, Attendance.AttendanceStatus.ABSENT);
        long lateCount = countAttendanceByStatusAndDate(tenantId, today, Attendance.AttendanceStatus.LATE);
        long halfDayCount = countAttendanceByStatusAndDate(tenantId, today, Attendance.AttendanceStatus.HALF_DAY);

        long total = presentCount + absentCount + lateCount + halfDayCount;
        if (total == 0) return 0.0;

        // Count present + late as present for percentage
        double percentage = ((presentCount + lateCount) * 100.0) / total;
        return Math.round(percentage * 100.0) / 100.0;
    }

    private long countAttendanceByStatusAndDate(String tenantId, LocalDate date, Attendance.AttendanceStatus status) {
        // Use JPA Specification to count attendance by status, date, and tenant
        return attendanceRepository.count((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("tenantId"), tenantId),
                        cb.equal(root.get("attendanceDate"), date),
                        cb.equal(root.get("status"), status)
                )
        );
    }
}
