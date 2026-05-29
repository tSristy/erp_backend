package org.enterprise.hr.service;

import org.enterprise.hr.dto.PayslipComponentDto;
import org.enterprise.hr.entity.PayslipComponent;
import org.enterprise.hr.repository.PayslipComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayslipComponentService {

    private final PayslipComponentRepository repository;

    public PayslipComponentDto create(PayslipComponentDto dto) {
        PayslipComponent entity = new PayslipComponent();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public PayslipComponentDto update(Long id, PayslipComponentDto dto) {
        PayslipComponent entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayslipComponent not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public PayslipComponentDto getById(Long id) {
        PayslipComponent entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayslipComponent not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<PayslipComponentDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(PayslipComponentDto dto, PayslipComponent entity) {
        // TODO: Map relation payslip manually from payslipId
        // TODO: Map relation salaryComponent manually from salaryComponentId
        entity.setType(dto.getType());
        entity.setAmount(dto.getAmount());
    }

    private PayslipComponentDto mapEntityToDto(PayslipComponent entity) {
        PayslipComponentDto dto = new PayslipComponentDto();
        dto.setId(entity.getId());
        if (entity.getPayslip() != null) {
            dto.setPayslipId(entity.getPayslip().getId());
        }
        if (entity.getSalaryComponent() != null) {
            dto.setSalaryComponentId(entity.getSalaryComponent().getId());
        }
        dto.setType(entity.getType());
        dto.setAmount(entity.getAmount());
        return dto;
    }
}
