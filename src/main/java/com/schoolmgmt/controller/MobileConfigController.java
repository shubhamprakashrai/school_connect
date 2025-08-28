package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.ConfigUpdateRequest;
import com.schoolmgmt.dto.response.MobileConfigResponse;
import com.schoolmgmt.service.MobileConfigService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = {"/api/config", "/config"}, produces = "application/json")
public class MobileConfigController {

    private final MobileConfigService service;

    public MobileConfigController(MobileConfigService service) {
        this.service = service;
    }

    @GetMapping("/mobile")
    public ResponseEntity<MobileConfigResponse> getMobileConfig(
            @RequestParam(required = false) String schoolId) {
        return ResponseEntity.ok(service.getConfig(schoolId));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateConfig(@Valid @RequestBody ConfigUpdateRequest request) {
        try {
            log.info("Updating config for schoolId: {}, scope: {}, key: {}", 
                    request.getSchoolId(), request.getScope(), request.getKey());
            
            service.updateConfig(request);
            return ResponseEntity.ok("Config updated successfully");
        } catch (Exception e) {
            log.error("Error updating config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error updating config: " + e.getMessage());
        }
    }
}
