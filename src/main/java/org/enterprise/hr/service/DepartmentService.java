package org.enterprise.hr.service;

import org.enterprise.hr.dto.DepartmentDto;
import org.enterprise.hr.entity.Department;
import org.enterprise.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentDto create(DepartmentDto dto) {
        Department entity = new Department();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public DepartmentDto getById(Long id) {
        Department entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(DepartmentDto dto, Department entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }

    private DepartmentDto mapEntityToDto(Department entity) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
