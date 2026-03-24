package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "master_data", indexes = {
        @Index(name = "idx_master_tenant_category", columnList = "tenant_id,category"),
        @Index(name = "idx_master_active", columnList = "tenant_id,category,is_active")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_master_tenant_category_value", columnNames = {"tenant_id", "category", "value"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterData extends BaseEntity {

    public enum Category {
        DESIGNATION,
        DEPARTMENT,
        EMPLOYEE_TYPE,
        CLASS_CATEGORY,
        FEE_CATEGORY,
        LEAVE_TYPE,
        SUBJECT_TYPE,
        QUALIFICATION
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "display_order")
    private int displayOrder = 0;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "is_default")
    private boolean isDefault = false;
}
