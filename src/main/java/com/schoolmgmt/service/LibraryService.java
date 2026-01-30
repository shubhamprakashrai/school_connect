package com.schoolmgmt.service;

import com.schoolmgmt.model.Book;
import com.schoolmgmt.model.BookIssue;
import com.schoolmgmt.repository.BookIssueRepository;
import com.schoolmgmt.repository.BookRepository;
import com.schoolmgmt.util.TenantContext;
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
public class LibraryService {

    private final BookRepository bookRepository;
    private final BookIssueRepository bookIssueRepository;

    // ===== Book Operations =====

    @Transactional
    public Book addBook(Book book) {
        String tenantId = TenantContext.getCurrentTenant();
        book.setTenantId(tenantId);
        log.info("Adding book: {} for tenant: {}", book.getTitle(), tenantId);
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(UUID id, Book updated) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found: " + id));

        if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
        if (updated.getAuthor() != null) existing.setAuthor(updated.getAuthor());
        if (updated.getIsbn() != null) existing.setIsbn(updated.getIsbn());
        if (updated.getCategory() != null) existing.setCategory(updated.getCategory());
        if (updated.getTotalCopies() != null) existing.setTotalCopies(updated.getTotalCopies());
        if (updated.getPublisher() != null) existing.setPublisher(updated.getPublisher());
        if (updated.getPublishYear() != null) existing.setPublishYear(updated.getPublishYear());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getLocation() != null) existing.setLocation(updated.getLocation());

        return bookRepository.save(existing);
    }

    @Transactional
    public void deleteBook(UUID id) {
        bookRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Book getBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String title) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookRepository.findByTitleContainingIgnoreCaseAndTenantId(title, tenantId);
    }

    // ===== Book Issue Operations =====

    @Transactional
    public BookIssue issueBook(BookIssue bookIssue) {
        String tenantId = TenantContext.getCurrentTenant();
        bookIssue.setTenantId(tenantId);
        bookIssue.setIssuedDate(LocalDate.now());
        bookIssue.setStatus(BookIssue.BookIssueStatus.ISSUED);

        // Decrease available copies
        Book book = getBookById(bookIssue.getBookId());
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies for book: " + book.getTitle());
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        log.info("Issuing book: {} to student: {}", bookIssue.getBookId(), bookIssue.getStudentId());
        return bookIssueRepository.save(bookIssue);
    }

    @Transactional
    public BookIssue returnBook(UUID issueId) {
        BookIssue issue = bookIssueRepository.findById(issueId)
                .orElseThrow(() -> new NoSuchElementException("Book issue not found: " + issueId));

        issue.setStatus(BookIssue.BookIssueStatus.RETURNED);
        issue.setReturnedDate(LocalDate.now());

        // Calculate fine if overdue
        if (LocalDate.now().isAfter(issue.getDueDate())) {
            long daysLate = LocalDate.now().toEpochDay() - issue.getDueDate().toEpochDay();
            issue.setFineAmount(daysLate * 1.0); // $1 per day
        }

        // Increase available copies
        Book book = getBookById(issue.getBookId());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        log.info("Book returned: {}", issueId);
        return bookIssueRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public Page<BookIssue> getIssuedBooks(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookIssueRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<BookIssue> getOverdueBooks() {
        String tenantId = TenantContext.getCurrentTenant();
        return bookIssueRepository.findByStatusAndDueDateBeforeAndTenantId(
                BookIssue.BookIssueStatus.ISSUED, LocalDate.now(), tenantId);
    }
}
