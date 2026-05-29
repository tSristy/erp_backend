package org.enterprise.hr.service;

import org.enterprise.hr.dto.PayrollProcessDto;
import org.enterprise.hr.entity.PayrollProcess;
import org.enterprise.hr.repository.PayrollProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollProcessService {

    private final PayrollProcessRepository repository;

    public PayrollProcessDto create(PayrollProcessDto dto) {
        PayrollProcess entity = new PayrollProcess();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public PayrollProcessDto update(Long id, PayrollProcessDto dto) {
        PayrollProcess entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollProcess not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public PayrollProcessDto getById(Long id) {
        PayrollProcess entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PayrollProcess not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<PayrollProcessDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(PayrollProcessDto dto, PayrollProcess entity) {
        entity.setProcessDate(dto.getProcessDate());
        entity.setProcessMonth(dto.getProcessMonth());
        entity.setProcessYear(dto.getProcessYear());
        entity.setStatus(dto.getStatus());
        entity.setTotalEarning(dto.getTotalEarning());
        entity.setTotalDeduction(dto.getTotalDeduction());
        entity.setNetPayment(dto.getNetPayment());
    }

    private PayrollProcessDto mapEntityToDto(PayrollProcess entity) {
        PayrollProcessDto dto = new PayrollProcessDto();
        dto.setId(entity.getId());
        dto.setProcessDate(entity.getProcessDate());
        dto.setProcessMonth(entity.getProcessMonth());
        dto.setProcessYear(entity.getProcessYear());
        dto.setStatus(entity.getStatus());
        dto.setTotalEarning(entity.getTotalEarning());
        dto.setTotalDeduction(entity.getTotalDeduction());
        dto.setNetPayment(entity.getNetPayment());
        return dto;
    }
}
