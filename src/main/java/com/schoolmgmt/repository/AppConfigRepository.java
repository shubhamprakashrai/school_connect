package com.schoolmgmt.repository;

import com.schoolmgmt.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    
    List<AppConfig> findBySchoolIdOrSchoolIdIsNull(String schoolId);
    
    List<AppConfig> findBySchoolIdAndScopeAndKey(String schoolId, String scope, String key);
    
    List<AppConfig> findBySchoolIdIsNullAndScopeAndKey(String scope, String key);
    
    Optional<AppConfig> findOneByScopeAndKeyAndSchoolId(String scope, String key, String schoolId);
    
    List<AppConfig> findByScopeAndKey(String scope, String key);
    
    @Modifying
    @Query("DELETE FROM AppConfig c WHERE c.scope = :scope AND c.key = :key AND (c.schoolId IS NULL OR c.schoolId = :schoolId)")
    int deleteByScopeAndKeyAndSchoolId(@Param("scope") String scope, 
                                    @Param("key") String key, 
                                    @Param("schoolId") String schoolId);
}
