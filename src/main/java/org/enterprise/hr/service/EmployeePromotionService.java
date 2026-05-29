package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeePromotionDto;
import org.enterprise.hr.entity.Designation;
import org.enterprise.hr.entity.Employee;
import org.enterprise.hr.entity.EmployeePromotion;
import org.enterprise.hr.repository.DesignationRepository;
import org.enterprise.hr.repository.EmployeeRepository;
import org.enterprise.hr.repository.EmployeePromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeePromotionService {

    private final EmployeePromotionRepository repository;
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;

    public EmployeePromotionDto create(EmployeePromotionDto dto) {
        EmployeePromotion entity = new EmployeePromotion();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeePromotionDto update(Long id, EmployeePromotionDto dto) {
        EmployeePromotion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeePromotion not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeePromotionDto getById(Long id) {
        EmployeePromotion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeePromotion not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeePromotionDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeePromotionDto dto, EmployeePromotion entity) {
        entity.setPromotionDate(dto.getPromotionDate());
        entity.setRemarks(dto.getRemarks());

        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            entity.setEmployee(employee);
        }

        if (dto.getPreviousDesignationId() != null) {
            Designation designation = designationRepository.findById(dto.getPreviousDesignationId())
                    .orElseThrow(() -> new RuntimeException("Previous Designation not found"));
            entity.setPreviousDesignation(designation);
        }

        if (dto.getNewDesignationId() != null) {
            Designation designation = designationRepository.findById(dto.getNewDesignationId())
                    .orElseThrow(() -> new RuntimeException("New Designation not found"));
            entity.setNewDesignation(designation);
        }
    }

    private EmployeePromotionDto mapEntityToDto(EmployeePromotion entity) {
        EmployeePromotionDto dto = new EmployeePromotionDto();
        dto.setId(entity.getId());
        dto.setPromotionDate(entity.getPromotionDate());
        dto.setRemarks(entity.getRemarks());

        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        if (entity.getPreviousDesignation() != null) {
            dto.setPreviousDesignationId(entity.getPreviousDesignation().getId());
        }
        if (entity.getNewDesignation() != null) {
            dto.setNewDesignationId(entity.getNewDesignation().getId());
        }
        return dto;
    }
}
