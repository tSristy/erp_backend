package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeLoanDto;
import org.enterprise.hr.dto.LoanInstallmentDto;
import org.enterprise.hr.entity.EmployeeLoan;
import org.enterprise.hr.entity.LoanInstallment;
import org.enterprise.hr.repository.EmployeeLoanRepository;
import org.enterprise.hr.repository.EmployeeRepository;
import org.enterprise.hr.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeLoanService {

    private final EmployeeLoanRepository repository;
    private final EmployeeRepository employeeRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    public EmployeeLoanDto create(EmployeeLoanDto dto) {
        EmployeeLoan entity = new EmployeeLoan();
        mapDtoToEntity(dto, entity);
        
        entity.setPaidMonths(0);
        entity.setStatus("Active");
        if (entity.getMonths() != null && entity.getMonths() > 0 && entity.getAmount() != null) {
            entity.setInstallment(entity.getAmount() / entity.getMonths());
        }
        
        entity = repository.save(entity);

        // Auto-generate installments
        if (entity.getMonths() != null && entity.getMonths() > 0) {
            LocalDate startDate = LocalDate.now();
            for (int i = 1; i <= entity.getMonths(); i++) {
                LoanInstallment installment = new LoanInstallment();
                installment.setLoan(entity);
                installment.setAmount(entity.getInstallment());
                installment.setDueDate(startDate.plusMonths(i));
                installment.setStatus("Pending");
                loanInstallmentRepository.save(installment);
            }
        }

        return getById(entity.getId());
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
        if (dto.getEmployeeId() != null) {
            entity.setEmployee(employeeRepository.findById(dto.getEmployeeId()).orElse(null));
        }
        entity.setAmount(dto.getAmount());
        if (dto.getInstallment() != null) {
            entity.setInstallment(dto.getInstallment());
        }
        entity.setMonths(dto.getMonths());
        if (dto.getPaidMonths() != null) {
            entity.setPaidMonths(dto.getPaidMonths());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
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
        dto.setStatus(entity.getStatus());
        
        if (entity.getInstallments() != null) {
            List<LoanInstallmentDto> installmentDtos = new ArrayList<>();
            for (LoanInstallment inst : entity.getInstallments()) {
                LoanInstallmentDto idto = new LoanInstallmentDto();
                idto.setId(inst.getId());
                idto.setLoanId(entity.getId());
                idto.setAmount(inst.getAmount());
                idto.setDueDate(inst.getDueDate());
                idto.setStatus(inst.getStatus());
                installmentDtos.add(idto);
            }
            dto.setInstallments(installmentDtos);
        }
        
        return dto;
    }
}
