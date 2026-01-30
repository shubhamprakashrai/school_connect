package com.schoolmgmt.controller;

import com.schoolmgmt.model.Book;
import com.schoolmgmt.model.BookIssue;
import com.schoolmgmt.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Library Management", description = "APIs for managing books and book issues")
public class LibraryController {

    private final LibraryService libraryService;

    // ===== Book Endpoints =====

    @PostMapping("/books")
    @Operation(summary = "Add a book to the library")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Book> addBook(@Valid @RequestBody Book book) {
        log.info("Adding book: {}", book.getTitle());
        Book created = libraryService.addBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/books")
    @Operation(summary = "Get paginated list of books")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Page<Book>> getAllBooks(
            @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getAllBooks(pageable));
    }

    @GetMapping("/books/{id}")
    @Operation(summary = "Get book by ID")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<Book> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    @PutMapping("/books/{id}")
    @Operation(summary = "Update book")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable UUID id, @Valid @RequestBody Book book) {
        return ResponseEntity.ok(libraryService.updateBook(id, book));
    }

    @DeleteMapping("/books/{id}")
    @Operation(summary = "Delete a book")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        libraryService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/search")
    @Operation(summary = "Search books by title")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN', 'SUPER_ADMIN', 'STUDENT')")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam String title) {
        return ResponseEntity.ok(libraryService.searchBooks(title));
    }

    // ===== Book Issue Endpoints =====

    @PostMapping("/issues")
    @Operation(summary = "Issue a book to a student")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BookIssue> issueBook(@Valid @RequestBody BookIssue bookIssue) {
        log.info("Issuing book: {} to student: {}", bookIssue.getBookId(), bookIssue.getStudentId());
        BookIssue issued = libraryService.issueBook(bookIssue);
        return ResponseEntity.status(HttpStatus.CREATED).body(issued);
    }

    @PutMapping("/issues/{id}/return")
    @Operation(summary = "Return a book")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BookIssue> returnBook(@PathVariable UUID id) {
        return ResponseEntity.ok(libraryService.returnBook(id));
    }

    @GetMapping("/issues")
    @Operation(summary = "Get paginated list of book issues")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<BookIssue>> getIssuedBooks(
            @PageableDefault(size = 20, sort = "issuedDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(libraryService.getIssuedBooks(pageable));
    }

    @GetMapping("/issues/overdue")
    @Operation(summary = "Get overdue book issues")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BookIssue>> getOverdueBooks() {
        return ResponseEntity.ok(libraryService.getOverdueBooks());
    }
}
