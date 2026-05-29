package org.enterprise.hr.service;

import org.enterprise.hr.dto.PayslipDto;
import org.enterprise.hr.entity.Payslip;
import org.enterprise.hr.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayslipService {

    private final PayslipRepository repository;

    public PayslipDto create(PayslipDto dto) {
        Payslip entity = new Payslip();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public PayslipDto update(Long id, PayslipDto dto) {
        Payslip entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payslip not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public PayslipDto getById(Long id) {
        Payslip entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payslip not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<PayslipDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(PayslipDto dto, Payslip entity) {
        // TODO: Map relation employee manually from employeeId
        // TODO: Map relation payrollProcess manually from payrollProcessId
        entity.setProcessMonth(dto.getProcessMonth());
        entity.setProcessYear(dto.getProcessYear());
        entity.setGrossSalary(dto.getGrossSalary());
        entity.setTotalEarning(dto.getTotalEarning());
        entity.setTotalDeduction(dto.getTotalDeduction());
        entity.setNetPayable(dto.getNetPayable());
        entity.setStatus(dto.getStatus());
    }

    private PayslipDto mapEntityToDto(Payslip entity) {
        PayslipDto dto = new PayslipDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        if (entity.getPayrollProcess() != null) {
            dto.setPayrollProcessId(entity.getPayrollProcess().getId());
        }
        dto.setProcessMonth(entity.getProcessMonth());
        dto.setProcessYear(entity.getProcessYear());
        dto.setGrossSalary(entity.getGrossSalary());
        dto.setTotalEarning(entity.getTotalEarning());
        dto.setTotalDeduction(entity.getTotalDeduction());
        dto.setNetPayable(entity.getNetPayable());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
