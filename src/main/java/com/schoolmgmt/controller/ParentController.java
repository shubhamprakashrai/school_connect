package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.ParentRequest;
import com.schoolmgmt.dto.response.ParentResponse;
import com.schoolmgmt.service.ParentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/parents")
@RequiredArgsConstructor
@Slf4j
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    public ResponseEntity<ParentResponse> createParent(@RequestBody ParentRequest request) {
        log.info("API called: Create parent");
        return ResponseEntity.ok(parentService.createParent(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParentResponse> getParent(@PathVariable UUID id) {
        log.info("API called: Get parent {}", id);
        return ResponseEntity.ok(parentService.getParent(id));
    }

    @GetMapping
    public ResponseEntity<List<ParentResponse>> getAllParents() {
        log.info("API called: Get all parents");
        return ResponseEntity.ok(parentService.getAllParents());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParentResponse> updateParent(
            @PathVariable UUID id,
            @RequestBody ParentRequest request) {

        log.info("API called: Update parent {}", id);
        return ResponseEntity.ok(parentService.updateParent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteParent(@PathVariable UUID id) {
        log.info("API called: Delete parent {}", id);
        parentService.deleteParent(id);
        return ResponseEntity.ok("Parent deleted successfully");
    }
}