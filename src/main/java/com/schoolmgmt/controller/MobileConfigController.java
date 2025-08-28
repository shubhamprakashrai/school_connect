package com.schoolmgmt.controller;

import com.schoolmgmt.dto.request.ConfigUpdateRequest;
import com.schoolmgmt.dto.response.MobileConfigResponse;
import com.schoolmgmt.service.MobileConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
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

    @PostMapping("/admin/update")
    public ResponseEntity<String> updateConfig(@RequestBody ConfigUpdateRequest request) {
        service.updateConfig(request);
        return ResponseEntity.ok("Config updated successfully");
    }
}
