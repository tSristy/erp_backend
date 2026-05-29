package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeTransferDto;
import org.enterprise.hr.entity.Department;
import org.enterprise.hr.entity.Employee;
import org.enterprise.hr.entity.EmployeeTransfer;
import org.enterprise.hr.repository.DepartmentRepository;
import org.enterprise.hr.repository.EmployeeRepository;
import org.enterprise.hr.repository.EmployeeTransferRepository;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeTransferService {

    private final EmployeeTransferRepository repository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeTransferDto create(EmployeeTransferDto dto) {
        EmployeeTransfer entity = new EmployeeTransfer();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeTransferDto update(Long id, EmployeeTransferDto dto) {
        EmployeeTransfer entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeTransfer not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeTransferDto getById(Long id) {
        EmployeeTransfer entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeTransfer not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeTransferDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeTransferDto dto, EmployeeTransfer entity) {
        entity.setTransferDate(dto.getTransferDate());
        entity.setRemarks(dto.getRemarks());

        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            entity.setEmployee(employee);
        }

        if (dto.getPreviousBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getPreviousBranchId())
                    .orElseThrow(() -> new RuntimeException("Previous Branch not found"));
            entity.setPreviousBranch(branch);
        }

        if (dto.getNewBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getNewBranchId())
                    .orElseThrow(() -> new RuntimeException("New Branch not found"));
            entity.setNewBranch(branch);
        }

        if (dto.getPreviousDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getPreviousDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Previous Department not found"));
            entity.setPreviousDepartment(department);
        }

        if (dto.getNewDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getNewDepartmentId())
                    .orElseThrow(() -> new RuntimeException("New Department not found"));
            entity.setNewDepartment(department);
        }
    }

    private EmployeeTransferDto mapEntityToDto(EmployeeTransfer entity) {
        EmployeeTransferDto dto = new EmployeeTransferDto();
        dto.setId(entity.getId());
        dto.setTransferDate(entity.getTransferDate());
        dto.setRemarks(entity.getRemarks());

        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        if (entity.getPreviousBranch() != null) {
            dto.setPreviousBranchId(entity.getPreviousBranch().getId());
        }
        if (entity.getNewBranch() != null) {
            dto.setNewBranchId(entity.getNewBranch().getId());
        }
        if (entity.getPreviousDepartment() != null) {
            dto.setPreviousDepartmentId(entity.getPreviousDepartment().getId());
        }
        if (entity.getNewDepartment() != null) {
            dto.setNewDepartmentId(entity.getNewDepartment().getId());
        }
        return dto;
    }
}
