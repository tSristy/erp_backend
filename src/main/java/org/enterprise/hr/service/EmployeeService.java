package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeDto;
import org.enterprise.hr.entity.Department;
import org.enterprise.hr.entity.Designation;
import org.enterprise.hr.entity.Employee;
import org.enterprise.hr.entity.Shift;
import org.enterprise.hr.repository.EmployeeRepository;
import org.enterprise.hr.repository.DepartmentRepository;
import org.enterprise.hr.repository.DesignationRepository;
import org.enterprise.hr.repository.ShiftRepository;
import org.enterprise.organization.entity.Branch;
import org.enterprise.organization.entity.Company;
import org.enterprise.organization.repository.BranchRepository;
import org.enterprise.organization.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final ShiftRepository shiftRepository;

    /**
     * CREATE
     */
    public EmployeeDto create(EmployeeDto dto) {

        validateEmployeeCode(dto.getEmployeeCode());

        Employee employee = new Employee();

        mapDtoToEntity(dto, employee);

        employee = employeeRepository.save(employee);

        return mapEntityToDto(employee);
    }

    /**
     * UPDATE
     */
    public EmployeeDto update(Long id, EmployeeDto dto) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id));

        if (!employee.getEmployeeCode().equals(dto.getEmployeeCode())) {
            validateEmployeeCode(dto.getEmployeeCode());
        }

        mapDtoToEntity(dto, employee);

        employee = employeeRepository.save(employee);

        return mapEntityToDto(employee);
    }

    /**
     * GET BY ID
     */
    @Transactional(readOnly = true)
    public EmployeeDto getById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id));

        return mapEntityToDto(employee);
    }

    /**
     * SEARCH
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDto> search(String keyword, Pageable pageable) {

        Page<Employee> page;

        if (keyword == null || keyword.isBlank()) {
            page = employeeRepository.findAll(pageable);
        } else {
            page = employeeRepository
                    .findByEmployeeCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                            keyword,
                            keyword,
                            pageable
                    );
        }

        return page.map(this::mapEntityToDto);
    }

    /**
     * DELETE (SOFT DELETE RECOMMENDED)
     */
    public void delete(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id));

        // recommended: soft delete
        employee.setActive(false);

        employeeRepository.save(employee);

        // hard delete if needed:
        // employeeRepository.delete(employee);
    }

    /**
     * VALIDATION
     */
    private void validateEmployeeCode(String employeeCode) {

        boolean exists = employeeRepository.existsByEmployeeCode(employeeCode);

        if (exists) {
            throw new RuntimeException(
                    "Employee code already exists: " + employeeCode);
        }
    }

    /**
     * DTO -> ENTITY
     */
    private void mapDtoToEntity(EmployeeDto dto, Employee employee) {

        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setFullName(dto.getFullName());

        employee.setGender(dto.getGender());
        employee.setMaritalStatus(dto.getMaritalStatus());

        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setJoiningDate(dto.getJoiningDate());

        employee.setMobile(dto.getMobile());
        employee.setEmail(dto.getEmail());

        employee.setPresentAddress(dto.getPresentAddress());
        employee.setPermanentAddress(dto.getPermanentAddress());

        employee.setActive(dto.getActive());

        // COMPANY
        if (dto.getCompanyId() != null) {

            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() ->
                            new RuntimeException("Company not found"));

            employee.setCompany(company);
        }

        // BRANCH
        if (dto.getBranchId() != null) {

            Branch branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() ->
                            new RuntimeException("Branch not found"));

            employee.setBranch(branch);
        }

        // DEPARTMENT
        if (dto.getDepartmentId() != null) {

            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() ->
                            new RuntimeException("Department not found"));

            employee.setDepartment(department);
        }

        // DESIGNATION
        if (dto.getDesignationId() != null) {

            Designation designation = designationRepository.findById(dto.getDesignationId())
                    .orElseThrow(() ->
                            new RuntimeException("Designation not found"));

            employee.setDesignation(designation);
        }

        // SHIFT
        if (dto.getDefaultShiftId() != null) {

            Shift shift = shiftRepository.findById(dto.getDefaultShiftId())
                    .orElseThrow(() ->
                            new RuntimeException("Shift not found"));

            employee.setDefaultShift(shift);
        }
    }

    /**
     * ENTITY -> DTO
     */
    private EmployeeDto mapEntityToDto(Employee employee) {

        EmployeeDto dto = new EmployeeDto();

        dto.setId(employee.getId());

        dto.setEmployeeCode(employee.getEmployeeCode());

        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setFullName(employee.getFullName());

        dto.setGender(employee.getGender());
        dto.setMaritalStatus(employee.getMaritalStatus());

        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setJoiningDate(employee.getJoiningDate());

        dto.setMobile(employee.getMobile());
        dto.setEmail(employee.getEmail());

        dto.setPresentAddress(employee.getPresentAddress());
        dto.setPermanentAddress(employee.getPermanentAddress());

        dto.setActive(employee.getActive());

        if (employee.getCompany() != null) {
            dto.setCompanyId(employee.getCompany().getId());
            dto.setCompanyName(employee.getCompany().getName());
        }

        if (employee.getBranch() != null) {
            dto.setBranchId(employee.getBranch().getId());
            dto.setBranchName(employee.getBranch().getName());
        }

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName()); // Wait, name or departmentName? Let me verify Department entity
        }

        if (employee.getDesignation() != null) {
            dto.setDesignationId(employee.getDesignation().getId());
            dto.setDesignationName(employee.getDesignation().getName()); // Wait, name or designationName? Let me verify Designation entity
        }

        if (employee.getDefaultShift() != null) {
            dto.setDefaultShiftId(employee.getDefaultShift().getId());
            dto.setDefaultShiftName(employee.getDefaultShift().getName()); // Wait, name or shiftName? Let me verify Shift entity
        }

        return dto;
    }
}
