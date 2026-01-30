package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hostel_rooms")
@ToString(callSuper = true)
public class HostelRoom extends BaseEntity {

    @Column(name = "hostel_id", nullable = false, length = 50)
    private String hostelId;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(name = "floor", length = 10)
    private String floor;

    @Column(name = "room_type", length = 20)
    private String roomType; // SINGLE, DOUBLE, DORMITORY

    @Column(name = "capacity")
    @Builder.Default
    private Integer capacity = 1;

    @Column(name = "occupied")
    @Builder.Default
    private Integer occupied = 0;

    @Column(name = "amenities", length = 500)
    private String amenities;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "AVAILABLE"; // AVAILABLE, FULL, MAINTENANCE

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
