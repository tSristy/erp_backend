package org.enterprise.hr.service;

import org.enterprise.hr.dto.AttendanceDto;
import org.enterprise.hr.entity.Attendance;
import org.enterprise.hr.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceDto create(AttendanceDto dto) {
        Attendance entity = new Attendance();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public AttendanceDto update(Long id, AttendanceDto dto) {
        Attendance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public AttendanceDto getById(Long id) {
        Attendance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(AttendanceDto dto, Attendance entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setAttendanceDate(dto.getAttendanceDate());
        entity.setInTime(dto.getInTime());
        entity.setOutTime(dto.getOutTime());
        // TODO: Map relation shift manually from shiftId
        entity.setLate(dto.getLate());
        entity.setEarlyOut(dto.getEarlyOut());
        entity.setAbsent(dto.getAbsent());
        entity.setWorkedHours(dto.getWorkedHours());
        entity.setOvertimeHours(dto.getOvertimeHours());
    }

    private AttendanceDto mapEntityToDto(Attendance entity) {
        AttendanceDto dto = new AttendanceDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setAttendanceDate(entity.getAttendanceDate());
        dto.setInTime(entity.getInTime());
        dto.setOutTime(entity.getOutTime());
        if (entity.getShift() != null) {
            dto.setShiftId(entity.getShift().getId());
        }
        dto.setLate(entity.getLate());
        dto.setEarlyOut(entity.getEarlyOut());
        dto.setAbsent(entity.getAbsent());
        dto.setWorkedHours(entity.getWorkedHours());
        dto.setOvertimeHours(entity.getOvertimeHours());
        return dto;
    }
}
