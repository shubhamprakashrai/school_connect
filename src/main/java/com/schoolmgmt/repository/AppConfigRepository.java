package com.schoolmgmt.repository;

import com.schoolmgmt.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {

    List<AppConfig> findBySchoolIdOrSchoolIdIsNull(String schoolId);
}
