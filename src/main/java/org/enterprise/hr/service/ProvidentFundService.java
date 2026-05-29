package org.enterprise.hr.service;

import org.enterprise.hr.dto.ProvidentFundDto;
import org.enterprise.hr.entity.ProvidentFund;
import org.enterprise.hr.repository.ProvidentFundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvidentFundService {

    private final ProvidentFundRepository repository;

    public ProvidentFundDto create(ProvidentFundDto dto) {
        ProvidentFund entity = new ProvidentFund();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public ProvidentFundDto update(Long id, ProvidentFundDto dto) {
        ProvidentFund entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProvidentFund not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public ProvidentFundDto getById(Long id) {
        ProvidentFund entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProvidentFund not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<ProvidentFundDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(ProvidentFundDto dto, ProvidentFund entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setEmployeeContribution(dto.getEmployeeContribution());
        entity.setEmployerContribution(dto.getEmployerContribution());
    }

    private ProvidentFundDto mapEntityToDto(ProvidentFund entity) {
        ProvidentFundDto dto = new ProvidentFundDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setEmployeeContribution(entity.getEmployeeContribution());
        dto.setEmployerContribution(entity.getEmployerContribution());
        return dto;
    }
}
