package com.schoolmgmt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmgmt.dto.request.ConfigUpdateRequest;
import com.schoolmgmt.dto.response.MobileConfigResponse;
import com.schoolmgmt.model.AppConfig;
import com.schoolmgmt.repository.AppConfigRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MobileConfigService {

    private final AppConfigRepository repository;
    private final ObjectMapper objectMapper;

    public MobileConfigService(AppConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public MobileConfigResponse getConfig(String schoolId) {
        List<AppConfig> configs = repository.findBySchoolIdOrSchoolIdIsNull(schoolId);

        Map<String, Map<String, Object>> grouped = configs.stream().collect(
                Collectors.groupingBy(
                        AppConfig::getScope,
                        Collectors.toMap(AppConfig::getKey, c -> parseValue(c.getValue()))
                )
        );

        return MobileConfigResponse.builder()
                .version("1.0.0")
                .lastUpdated(Instant.now().toString())
                .features(grouped.getOrDefault("features", Collections.emptyMap()))
                .ui(grouped.getOrDefault("ui", Collections.emptyMap()))
                .runtime(grouped.getOrDefault("runtime", Collections.emptyMap()))
                .build();
    }

    public void updateConfig(ConfigUpdateRequest request) {
        AppConfig config = AppConfig.builder()
                .schoolId(request.getSchoolId())
                .scope(request.getScope())
                .key(request.getKey())
                .value(writeValue(request.getValue()))
                .updatedAt(Instant.now())
                .build();
        repository.save(config);
    }

    private Object parseValue(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return json;
        }
    }

    private String writeValue(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON value");
        }
    }
}
