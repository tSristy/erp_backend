package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeLoanDto;
import org.enterprise.hr.entity.EmployeeLoan;
import org.enterprise.hr.repository.EmployeeLoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeLoanService {

    private final EmployeeLoanRepository repository;

    public EmployeeLoanDto create(EmployeeLoanDto dto) {
        EmployeeLoan entity = new EmployeeLoan();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeLoanDto update(Long id, EmployeeLoanDto dto) {
        EmployeeLoan entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeLoan not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeLoanDto getById(Long id) {
        EmployeeLoan entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeLoan not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeLoanDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeLoanDto dto, EmployeeLoan entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setAmount(dto.getAmount());
        entity.setInstallment(dto.getInstallment());
        entity.setMonths(dto.getMonths());
        entity.setPaidMonths(dto.getPaidMonths());
    }

    private EmployeeLoanDto mapEntityToDto(EmployeeLoan entity) {
        EmployeeLoanDto dto = new EmployeeLoanDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setAmount(entity.getAmount());
        dto.setInstallment(entity.getInstallment());
        dto.setMonths(entity.getMonths());
        dto.setPaidMonths(entity.getPaidMonths());
        return dto;
    }
}
