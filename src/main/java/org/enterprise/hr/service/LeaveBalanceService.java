package org.enterprise.hr.service;

import org.enterprise.hr.dto.LeaveBalanceDto;
import org.enterprise.hr.entity.LeaveBalance;
import org.enterprise.hr.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveBalanceService {

    private final LeaveBalanceRepository repository;

    public LeaveBalanceDto create(LeaveBalanceDto dto) {
        LeaveBalance entity = new LeaveBalance();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public LeaveBalanceDto update(Long id, LeaveBalanceDto dto) {
        LeaveBalance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveBalance not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public LeaveBalanceDto getById(Long id) {
        LeaveBalance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("LeaveBalance not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<LeaveBalanceDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(LeaveBalanceDto dto, LeaveBalance entity) {
        // TODO: Map relation employee manually from employeeId
        // TODO: Map relation leaveType manually from leaveTypeId
        entity.setTotalDays(dto.getTotalDays());
        entity.setUsedDays(dto.getUsedDays());
        entity.setRemainingDays(dto.getRemainingDays());
        entity.setBalanceDate(dto.getBalanceDate());
    }

    private LeaveBalanceDto mapEntityToDto(LeaveBalance entity) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        if (entity.getLeaveType() != null) {
            dto.setLeaveTypeId(entity.getLeaveType().getId());
        }
        dto.setTotalDays(entity.getTotalDays());
        dto.setUsedDays(entity.getUsedDays());
        dto.setRemainingDays(entity.getRemainingDays());
        dto.setBalanceDate(entity.getBalanceDate());
        return dto;
    }
}
