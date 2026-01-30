package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TransportRoute entity representing a school bus route.
 */
@Entity
@Table(name = "transport_routes",
       indexes = {
           @Index(name = "idx_transport_route_tenant", columnList = "tenant_id"),
           @Index(name = "idx_transport_route_number", columnList = "route_number"),
           @Index(name = "idx_transport_route_active", columnList = "is_active"),
           @Index(name = "idx_transport_route_vehicle", columnList = "vehicle_number")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class TransportRoute implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "route_name", nullable = false, length = 200)
    private String routeName;

    @Column(name = "route_number", nullable = false, length = 50)
    private String routeNumber;

    @Column(name = "start_point", nullable = false, length = 300)
    private String startPoint;

    @Column(name = "end_point", nullable = false, length = 300)
    private String endPoint;

    @ElementCollection
    @CollectionTable(name = "transport_route_stops", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "stop_name")
    @Builder.Default
    private List<String> stops = new ArrayList<>();

    @Column(name = "driver_name", length = 200)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "vehicle_number", nullable = false, length = 50)
    private String vehicleNumber;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(name = "capacity")
    @Builder.Default
    private Integer capacity = 40;

    @Column(name = "current_student_count")
    @Builder.Default
    private Integer currentStudentCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
