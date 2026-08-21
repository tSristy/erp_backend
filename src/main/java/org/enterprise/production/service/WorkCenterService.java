package org.enterprise.production.service;

import org.enterprise.production.dto.WorkCenterDto;
import org.enterprise.production.entity.WorkCenter;
import org.enterprise.production.repository.WorkCenterRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkCenterService {

    private final WorkCenterRepository repository;

    public WorkCenterService(WorkCenterRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<WorkCenterDto> findAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkCenterDto findById(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public WorkCenterDto save(WorkCenterDto dto) {
        Long companyId = org.enterprise.common.util.TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        WorkCenter entity = convertToEntity(dto);
        entity.setCompanyId(companyId);

        WorkCenter saved = repository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private WorkCenterDto convertToDTO(WorkCenter entity) {
        WorkCenterDto dto = new WorkCenterDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private WorkCenter convertToEntity(WorkCenterDto dto) {
        WorkCenter entity = new WorkCenter();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
