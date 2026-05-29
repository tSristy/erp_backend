package org.enterprise.hr.service;

import org.enterprise.hr.dto.LeaveApplicationDto;
import org.enterprise.hr.entity.LeaveApplication;
import org.enterprise.hr.repository.LeaveApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveApplicationService {

    private final LeaveApplicationRepository repository;

    public LeaveApplicationDto create(LeaveApplicationDto dto) {
        LeaveApplication entity = new LeaveApplication();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public LeaveApplicationDto update(Long id, LeaveApplicationDto dto) {
        LeaveApplication entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public LeaveApplicationDto getById(Long id) {
        LeaveApplication entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveApplication not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<LeaveApplicationDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(LeaveApplicationDto dto, LeaveApplication entity) {
        entity.setFromDate(dto.getFromDate());
        entity.setToDate(dto.getToDate());
        entity.setDays(dto.getDays());
        entity.setReason(dto.getReason());
        entity.setStatus(dto.getStatus());
    }

    private LeaveApplicationDto mapEntityToDto(LeaveApplication entity) {
        LeaveApplicationDto dto = new LeaveApplicationDto();
        dto.setId(entity.getId());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());
        dto.setDays(entity.getDays());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
