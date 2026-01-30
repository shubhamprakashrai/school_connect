package com.schoolmgmt.service;

import com.schoolmgmt.exception.ResourceNotFoundException;
import com.schoolmgmt.model.Hostel;
import com.schoolmgmt.model.HostelAllocation;
import com.schoolmgmt.model.HostelRoom;
import com.schoolmgmt.repository.HostelAllocationRepository;
import com.schoolmgmt.repository.HostelRepository;
import com.schoolmgmt.repository.HostelRoomRepository;
import com.schoolmgmt.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HostelService {

    private final HostelRepository hostelRepository;
    private final HostelRoomRepository roomRepository;
    private final HostelAllocationRepository allocationRepository;

    // Hostel CRUD
    public Hostel createHostel(Hostel hostel) {
        String tenantId = TenantContext.requireCurrentTenant();
        hostel.setTenantId(tenantId);
        Hostel saved = hostelRepository.save(hostel);
        log.info("Hostel created: {}", saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public Hostel getHostelById(UUID id) {
        String tenantId = TenantContext.requireCurrentTenant();
        Hostel hostel = hostelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel", "id", id));
        if (!hostel.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Hostel", "id", id);
        }
        return hostel;
    }

    @Transactional(readOnly = true)
    public Page<Hostel> getAllHostels(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return hostelRepository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }

    public Hostel updateHostel(UUID id, Hostel updates) {
        Hostel existing = getHostelById(id);
        if (updates.getName() != null) existing.setName(updates.getName());
        if (updates.getWardenName() != null) existing.setWardenName(updates.getWardenName());
        if (updates.getWardenContact() != null) existing.setWardenContact(updates.getWardenContact());
        if (updates.getCapacity() != null) existing.setCapacity(updates.getCapacity());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        return hostelRepository.save(existing);
    }

    public void deleteHostel(UUID id) {
        Hostel hostel = getHostelById(id);
        hostel.softDelete(TenantContext.requireCurrentTenant());
        hostelRepository.save(hostel);
        log.info("Hostel deleted: {}", id);
    }

    // Room CRUD
    public HostelRoom createRoom(HostelRoom room) {
        String tenantId = TenantContext.requireCurrentTenant();
        room.setTenantId(tenantId);
        HostelRoom saved = roomRepository.save(room);
        log.info("Hostel room created: {} in hostel: {}", saved.getRoomNumber(), saved.getHostelId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<HostelRoom> getRoomsByHostel(String hostelId, Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return roomRepository.findByHostelIdAndTenantIdAndIsActiveTrue(hostelId, tenantId, pageable);
    }

    // Allocation CRUD
    public HostelAllocation createAllocation(HostelAllocation allocation) {
        String tenantId = TenantContext.requireCurrentTenant();
        allocation.setTenantId(tenantId);
        HostelAllocation saved = allocationRepository.save(allocation);
        log.info("Hostel allocation created for student: {}", saved.getStudentId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<HostelAllocation> getAllAllocations(Pageable pageable) {
        String tenantId = TenantContext.requireCurrentTenant();
        return allocationRepository.findByTenantIdAndIsActiveTrue(tenantId, pageable);
    }
}
