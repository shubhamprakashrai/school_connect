package com.schoolmgmt.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hostels")
@ToString(callSuper = true)
public class Hostel extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "hostel_type", length = 20)
    private String hostelType; // BOYS, GIRLS, CO_ED

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "warden_name", length = 200)
    private String wardenName;

    @Column(name = "warden_contact", length = 20)
    private String wardenContact;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "occupied")
    @Builder.Default
    private Integer occupied = 0;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
