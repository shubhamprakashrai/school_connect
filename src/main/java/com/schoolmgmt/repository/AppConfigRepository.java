package com.schoolmgmt.repository;

import com.schoolmgmt.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    
    List<AppConfig> findBySchoolIdOrSchoolIdIsNull(String schoolId);
    
    List<AppConfig> findBySchoolIdAndScopeAndKey(String schoolId, String scope, String key);
    
    List<AppConfig> findBySchoolIdIsNullAndScopeAndKey(String scope, String key);
    
    Optional<AppConfig> findOneByScopeAndKeyAndSchoolId(String scope, String key, String schoolId);
    
    List<AppConfig> findByScopeAndKey(String scope, String key);
}
