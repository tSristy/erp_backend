package org.enterprise.hr.service;

import org.enterprise.hr.dto.LeaveTypeDto;
import org.enterprise.hr.entity.LeaveType;
import org.enterprise.hr.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveTypeService {

    private final LeaveTypeRepository repository;

    public LeaveTypeDto create(LeaveTypeDto dto) {
        LeaveType entity = new LeaveType();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public LeaveTypeDto update(Long id, LeaveTypeDto dto) {
        LeaveType entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public LeaveTypeDto getById(Long id) {
        LeaveType entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<LeaveTypeDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(LeaveTypeDto dto, LeaveType entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDays(dto.getDays());
        entity.setType(dto.getType());
    }

    private LeaveTypeDto mapEntityToDto(LeaveType entity) {
        LeaveTypeDto dto = new LeaveTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setDays(entity.getDays());
        dto.setType(entity.getType());
        return dto;
    }
}
