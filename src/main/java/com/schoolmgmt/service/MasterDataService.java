package com.schoolmgmt.service;

import com.schoolmgmt.dto.request.MasterDataRequest;
import com.schoolmgmt.dto.response.MasterDataResponse;
import com.schoolmgmt.exception.BusinessException;
import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.MasterData;
import com.schoolmgmt.repository.MasterDataRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MasterDataService {

    private final MasterDataRepository masterDataRepository;

    public MasterDataResponse createEntry(MasterDataRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();
        MasterData.Category category = MasterData.Category.valueOf(request.getCategory());

        if (masterDataRepository.existsByTenantIdAndCategoryAndValue(tenantId, category, request.getValue())) {
            throw new BusinessException("Entry already exists for category '" + request.getCategory()
                    + "' with value '" + request.getValue() + "'");
        }

        MasterData entity = MasterData.builder()
                .category(category)
                .value(request.getValue())
                .label(request.getLabel())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .isActive(true)
                .isDefault(false)
                .build();
        entity.setTenantId(tenantId);

        MasterData saved = masterDataRepository.save(entity);
        log.info("Created master data entry: {} / {} for tenant: {}", category, request.getValue(), tenantId);
        return toResponse(saved);
    }

    public MasterDataResponse updateEntry(UUID id, MasterDataRequest request) {
        String tenantId = TenantContext.requireCurrentTenant();

        MasterData entity = masterDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData", "id", id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("MasterData", "id", id);
        }

        // If value is changing, check uniqueness
        MasterData.Category category = MasterData.Category.valueOf(request.getCategory());
        if (!entity.getValue().equals(request.getValue()) || !entity.getCategory().equals(category)) {
            if (masterDataRepository.existsByTenantIdAndCategoryAndValue(tenantId, category, request.getValue())) {
                throw new BusinessException("Entry already exists for category '" + request.getCategory()
                        + "' with value '" + request.getValue() + "'");
            }
        }

        entity.setCategory(category);
        entity.setValue(request.getValue());
        entity.setLabel(request.getLabel());
        entity.setDescription(request.getDescription());
        entity.setDisplayOrder(request.getDisplayOrder());

        MasterData saved = masterDataRepository.save(entity);
        log.info("Updated master data entry: {} for tenant: {}", id, tenantId);
        return toResponse(saved);
    }

    public void deleteEntry(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();

        MasterData entity = masterDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterData", "id", id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("MasterData", "id", id);
        }

        entity.setActive(false);
        masterDataRepository.save(entity);
        log.info("Soft deleted master data entry: {} for tenant: {}", id, tenantId);
    }

    @Transactional(readOnly = true)
    public List<MasterDataResponse> getByCategory(String category) {
        String tenantId = TenantContext.requireCurrentTenant();
        MasterData.Category cat = MasterData.Category.valueOf(category);

        return masterDataRepository
                .findByTenantIdAndCategoryAndIsActiveTrueOrderByDisplayOrder(tenantId, cat)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, List<MasterDataResponse>> getAllForTenant() {
        String tenantId = TenantContext.requireCurrentTenant();

        return masterDataRepository
                .findByTenantIdAndIsActiveTrueOrderByCategoryAscDisplayOrderAsc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.groupingBy(MasterDataResponse::getCategory));
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return Arrays.stream(MasterData.Category.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public void seedDefaultData(String tenantId) {
        log.info("Seeding default master data for tenant: {}", tenantId);

        seedCategory(tenantId, MasterData.Category.DESIGNATION, new String[][]{
                {"PRINCIPAL", "Principal"},
                {"VICE_PRINCIPAL", "Vice Principal"},
                {"HEAD_OF_DEPARTMENT", "Head of Department"},
                {"SENIOR_TEACHER", "Senior Teacher"},
                {"TEACHER", "Teacher"},
                {"LAB_ASSISTANT", "Lab Assistant"},
                {"LIBRARIAN", "Librarian"},
                {"PHYSICAL_TRAINER", "Physical Trainer"},
                {"COUNSELOR", "Counselor"}
        });

        seedCategory(tenantId, MasterData.Category.DEPARTMENT, new String[][]{
                {"SCIENCE", "Science"},
                {"MATHEMATICS", "Mathematics"},
                {"ENGLISH", "English"},
                {"HINDI", "Hindi"},
                {"SOCIAL_STUDIES", "Social Studies"},
                {"COMPUTER_SCIENCE", "Computer Science"},
                {"PHYSICAL_EDUCATION", "Physical Education"},
                {"ARTS", "Arts"},
                {"MUSIC", "Music"},
                {"COMMERCE", "Commerce"}
        });

        seedCategory(tenantId, MasterData.Category.EMPLOYEE_TYPE, new String[][]{
                {"PERMANENT", "Permanent"},
                {"CONTRACTUAL", "Contractual"},
                {"PART_TIME", "Part-time"},
                {"GUEST_FACULTY", "Guest Faculty"},
                {"INTERN", "Intern"}
        });

        seedCategory(tenantId, MasterData.Category.CLASS_CATEGORY, new String[][]{
                {"PRIMARY", "Primary (1-5)"},
                {"MIDDLE", "Middle (6-8)"},
                {"SECONDARY", "Secondary (9-10)"},
                {"SENIOR_SECONDARY", "Senior Secondary (11-12)"}
        });

        seedCategory(tenantId, MasterData.Category.SUBJECT_TYPE, new String[][]{
                {"MATHEMATICS", "Mathematics"},
                {"PHYSICS", "Physics"},
                {"CHEMISTRY", "Chemistry"},
                {"BIOLOGY", "Biology"},
                {"ENGLISH", "English"},
                {"HINDI", "Hindi"},
                {"SANSKRIT", "Sanskrit"},
                {"HISTORY", "History"},
                {"GEOGRAPHY", "Geography"},
                {"POLITICAL_SCIENCE", "Political Science"},
                {"ECONOMICS", "Economics"},
                {"BUSINESS_STUDIES", "Business Studies"},
                {"ACCOUNTANCY", "Accountancy"},
                {"COMPUTER_SCIENCE", "Computer Science"},
                {"PHYSICAL_EDUCATION", "Physical Education"},
                {"ART", "Art"},
                {"MUSIC", "Music"}
        });

        seedCategory(tenantId, MasterData.Category.QUALIFICATION, new String[][]{
                {"B_ED", "B.Ed"},
                {"M_ED", "M.Ed"},
                {"B_SC", "B.Sc"},
                {"M_SC", "M.Sc"},
                {"B_A", "B.A"},
                {"M_A", "M.A"},
                {"B_TECH", "B.Tech"},
                {"M_TECH", "M.Tech"},
                {"PHD", "Ph.D"},
                {"D_EL_ED", "D.El.Ed"},
                {"B_COM", "B.Com"},
                {"M_COM", "M.Com"},
                {"MBA", "MBA"},
                {"OTHER", "Other"}
        });

        log.info("Completed seeding default master data for tenant: {}", tenantId);
    }

    private void seedCategory(String tenantId, MasterData.Category category, String[][] entries) {
        for (int i = 0; i < entries.length; i++) {
            String value = entries[i][0];
            String label = entries[i][1];

            if (!masterDataRepository.existsByTenantIdAndCategoryAndValue(tenantId, category, value)) {
                MasterData entity = MasterData.builder()
                        .category(category)
                        .value(value)
                        .label(label)
                        .displayOrder(i + 1)
                        .isActive(true)
                        .isDefault(true)
                        .build();
                entity.setTenantId(tenantId);
                masterDataRepository.save(entity);
            }
        }
    }

    private MasterDataResponse toResponse(MasterData entity) {
        return MasterDataResponse.builder()
                .id(entity.getId())
                .category(entity.getCategory().name())
                .value(entity.getValue())
                .label(entity.getLabel())
                .description(entity.getDescription())
                .displayOrder(entity.getDisplayOrder())
                .isActive(entity.isActive())
                .isDefault(entity.isDefault())
                .build();
    }
}
