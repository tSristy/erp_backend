package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeIncrementDto;
import org.enterprise.hr.entity.Employee;
import org.enterprise.hr.entity.EmployeeIncrement;
import org.enterprise.hr.repository.EmployeeRepository;
import org.enterprise.hr.repository.EmployeeIncrementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeIncrementService {

    private final EmployeeIncrementRepository repository;
    private final EmployeeRepository employeeRepository;

    public EmployeeIncrementDto create(EmployeeIncrementDto dto) {
        EmployeeIncrement entity = new EmployeeIncrement();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeIncrementDto update(Long id, EmployeeIncrementDto dto) {
        EmployeeIncrement entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeIncrement not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeIncrementDto getById(Long id) {
        EmployeeIncrement entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeIncrement not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeIncrementDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeIncrementDto dto, EmployeeIncrement entity) {
        entity.setPreviousSalary(dto.getPreviousSalary());
        entity.setNewSalary(dto.getNewSalary());
        entity.setIncrementDate(dto.getIncrementDate());
        entity.setRemarks(dto.getRemarks());

        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            entity.setEmployee(employee);
        }
    }

    private EmployeeIncrementDto mapEntityToDto(EmployeeIncrement entity) {
        EmployeeIncrementDto dto = new EmployeeIncrementDto();
        dto.setId(entity.getId());
        dto.setPreviousSalary(entity.getPreviousSalary());
        dto.setNewSalary(entity.getNewSalary());
        dto.setIncrementDate(entity.getIncrementDate());
        dto.setRemarks(entity.getRemarks());

        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        return dto;
    }
}
