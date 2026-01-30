package com.schoolmgmt.repository;

import com.schoolmgmt.model.BookIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookIssueRepository extends JpaRepository<BookIssue, UUID> {

    Page<BookIssue> findByTenantId(String tenantId, Pageable pageable);

    List<BookIssue> findByStudentIdAndTenantId(UUID studentId, String tenantId);

    List<BookIssue> findByBookIdAndStatusAndTenantId(UUID bookId, BookIssue.BookIssueStatus status, String tenantId);

    List<BookIssue> findByStatusAndDueDateBeforeAndTenantId(BookIssue.BookIssueStatus status, LocalDate date, String tenantId);

    Page<BookIssue> findByStatusAndTenantId(BookIssue.BookIssueStatus status, String tenantId, Pageable pageable);
}
