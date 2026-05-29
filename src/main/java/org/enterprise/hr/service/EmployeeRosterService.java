package org.enterprise.hr.service;

import org.enterprise.hr.dto.EmployeeRosterDto;
import org.enterprise.hr.entity.EmployeeRoster;
import org.enterprise.hr.repository.EmployeeRosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeRosterService {

    private final EmployeeRosterRepository repository;

    public EmployeeRosterDto create(EmployeeRosterDto dto) {
        EmployeeRoster entity = new EmployeeRoster();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public EmployeeRosterDto update(Long id, EmployeeRosterDto dto) {
        EmployeeRoster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeRoster not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public EmployeeRosterDto getById(Long id) {
        EmployeeRoster entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("EmployeeRoster not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeRosterDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(EmployeeRosterDto dto, EmployeeRoster entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setDutyDate(dto.getDutyDate());
        // TODO: Map relation shift manually from shiftId
        entity.setHoliday(dto.getHoliday());
        entity.setWeeklyOff(dto.getWeeklyOff());
    }

    private EmployeeRosterDto mapEntityToDto(EmployeeRoster entity) {
        EmployeeRosterDto dto = new EmployeeRosterDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setDutyDate(entity.getDutyDate());
        if (entity.getShift() != null) {
            dto.setShiftId(entity.getShift().getId());
        }
        dto.setHoliday(entity.getHoliday());
        dto.setWeeklyOff(entity.getWeeklyOff());
        return dto;
    }
}
