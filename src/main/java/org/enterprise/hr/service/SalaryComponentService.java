package org.enterprise.hr.service;

import org.enterprise.hr.dto.SalaryComponentDto;
import org.enterprise.hr.entity.SalaryComponent;
import org.enterprise.hr.repository.SalaryComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaryComponentService {

    private final SalaryComponentRepository repository;

    public SalaryComponentDto create(SalaryComponentDto dto) {
        SalaryComponent entity = new SalaryComponent();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public SalaryComponentDto update(Long id, SalaryComponentDto dto) {
        SalaryComponent entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryComponent not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public SalaryComponentDto getById(Long id) {
        SalaryComponent entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryComponent not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<SalaryComponentDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(SalaryComponentDto dto, SalaryComponent entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setType(dto.getType());
        entity.setCalculationMethod(dto.getCalculationMethod());
        entity.setAmount(dto.getAmount());
    }

    private SalaryComponentDto mapEntityToDto(SalaryComponent entity) {
        SalaryComponentDto dto = new SalaryComponentDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setType(entity.getType());
        dto.setCalculationMethod(entity.getCalculationMethod());
        dto.setAmount(entity.getAmount());
        return dto;
    }
}
