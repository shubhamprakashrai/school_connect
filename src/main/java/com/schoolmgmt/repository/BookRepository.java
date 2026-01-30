package com.schoolmgmt.repository;

import com.schoolmgmt.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    Page<Book> findByTenantId(String tenantId, Pageable pageable);

    List<Book> findByTitleContainingIgnoreCaseAndTenantId(String title, String tenantId);

    List<Book> findByCategoryAndTenantId(String category, String tenantId);

    List<Book> findByAvailableCopiesGreaterThanAndTenantId(int copies, String tenantId);
}
