package org.enterprise.hr.service;

import org.enterprise.hr.dto.LoanInstallmentDto;
import org.enterprise.hr.entity.LoanInstallment;
import org.enterprise.hr.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanInstallmentService {

    private final LoanInstallmentRepository repository;

    public LoanInstallmentDto create(LoanInstallmentDto dto) {
        LoanInstallment entity = new LoanInstallment();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public LoanInstallmentDto update(Long id, LoanInstallmentDto dto) {
        LoanInstallment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LoanInstallment not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public LoanInstallmentDto getById(Long id) {
        LoanInstallment entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LoanInstallment not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<LoanInstallmentDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(LoanInstallmentDto dto, LoanInstallment entity) {
        // TODO: Map relation loan manually from loanId
        entity.setDueDate(dto.getDueDate());
        entity.setAmount(dto.getAmount());
    }

    private LoanInstallmentDto mapEntityToDto(LoanInstallment entity) {
        LoanInstallmentDto dto = new LoanInstallmentDto();
        dto.setId(entity.getId());
        if (entity.getLoan() != null) {
            dto.setLoanId(entity.getLoan().getId());
        }
        dto.setDueDate(entity.getDueDate());
        dto.setAmount(entity.getAmount());
        return dto;
    }
}
