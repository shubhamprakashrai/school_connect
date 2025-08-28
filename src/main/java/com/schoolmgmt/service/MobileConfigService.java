package com.schoolmgmt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmgmt.dto.request.ConfigUpdateRequest;
import com.schoolmgmt.dto.response.MobileConfigResponse;
import com.schoolmgmt.model.AppConfig;
import com.schoolmgmt.repository.AppConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;


@Slf4j
@Service
public class MobileConfigService {

    private final AppConfigRepository repository;
    private final ObjectMapper objectMapper;

    public MobileConfigService(AppConfigRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public MobileConfigResponse getConfig(String schoolId) {
        // Get all configs for the school or global configs
        List<AppConfig> configs = repository.findBySchoolIdOrSchoolIdIsNull(schoolId);
        
        // Group by scope and key, keeping the most specific config (school-specific overrides global)
        Map<String, Map<String, Object>> grouped = new HashMap<>();
        
        for (AppConfig config : configs) {
            String scope = config.getScope();
            String key = config.getKey();
            
            // Only override if this is a more specific config (school-specific) or if we haven't seen this key yet
            if (!grouped.containsKey(scope) || 
                !grouped.get(scope).containsKey(key) || 
                (config.getSchoolId() != null && config.getSchoolId().equals(schoolId))) {
                
                // Parse the JSON value
                Object value = parseValue(config.getValue());
                
                // Add to the appropriate scope
                grouped.computeIfAbsent(scope, k -> new HashMap<>())
                      .put(key, value);
            }
        }
        
        // Build the response with all scopes, even if empty
        return MobileConfigResponse.builder()
                .version("1.0.0")
                .lastUpdated(Instant.now().toString())
                .features(grouped.getOrDefault("features", Collections.emptyMap()))
                .ui(grouped.getOrDefault("ui", Collections.emptyMap()))
                .runtime(grouped.getOrDefault("runtime", Collections.emptyMap()))
                .build();
    }

    @Transactional
    public boolean deleteConfig(String scope, String key, String schoolId) {
        log.info("Deleting config for schoolId: {}, scope: {}, key: {}", schoolId, scope, key);
        return repository.deleteByScopeAndKeyAndSchoolId(scope, key, schoolId) > 0;
    }

    @Transactional
    public void updateConfig(ConfigUpdateRequest request) {
        try {
            log.info("Updating config for schoolId: {}, scope: {}, key: {}", 
                    request.getSchoolId(), request.getScope(), request.getKey());
            
            // Find existing config or create new one
            AppConfig config = repository.findOneByScopeAndKeyAndSchoolId(
                    request.getScope(), 
                    request.getKey(),
                    request.getSchoolId()
            ).orElseGet(() -> AppConfig.builder()
                    .schoolId(request.getSchoolId())
                    .scope(request.getScope())
                    .key(request.getKey())
                    .build());
            
            // Convert the request value to JsonNode
            JsonNode valueNode = objectMapper.valueToTree(request.getValue());
            config.setValue(valueNode);
            config.setUpdatedAt(Instant.now());
            
            // Save the config
            repository.save(config);
            log.info("Config updated successfully: {}", config);
            
        } catch (Exception e) {
            log.error("Error updating config: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update configuration: " + e.getMessage(), e);
        }
    }

    private Object parseValue(JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        try {
            if (jsonNode.isObject()) {
                return objectMapper.convertValue(jsonNode, Map.class);
            } else if (jsonNode.isArray()) {
                return objectMapper.convertValue(jsonNode, List.class);
            } else if (jsonNode.isTextual()) {
                return jsonNode.asText();
            } else if (jsonNode.isNumber()) {
                return jsonNode.numberValue();
            } else if (jsonNode.isBoolean()) {
                return jsonNode.booleanValue();
            }
            return jsonNode.toString();
        } catch (Exception e) {
            log.warn("Error parsing JSON value: {}", e.getMessage());
            return jsonNode.toString();
        }
    }

}
