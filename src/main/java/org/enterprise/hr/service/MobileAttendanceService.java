package org.enterprise.hr.service;

import org.enterprise.hr.dto.MobileAttendanceDto;
import org.enterprise.hr.entity.MobileAttendance;
import org.enterprise.hr.repository.MobileAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MobileAttendanceService {

    private final MobileAttendanceRepository repository;

    public MobileAttendanceDto create(MobileAttendanceDto dto) {
        MobileAttendance entity = new MobileAttendance();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public MobileAttendanceDto update(Long id, MobileAttendanceDto dto) {
        MobileAttendance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MobileAttendance not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public MobileAttendanceDto getById(Long id) {
        MobileAttendance entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("MobileAttendance not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<MobileAttendanceDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(MobileAttendanceDto dto, MobileAttendance entity) {
        // TODO: Map relation employee manually from employeeId
        entity.setAttendanceTime(dto.getAttendanceTime());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setAttendanceType(dto.getAttendanceType());
    }

    private MobileAttendanceDto mapEntityToDto(MobileAttendance entity) {
        MobileAttendanceDto dto = new MobileAttendanceDto();
        dto.setId(entity.getId());
        if (entity.getEmployee() != null) {
            dto.setEmployeeId(entity.getEmployee().getId());
        }
        dto.setAttendanceTime(entity.getAttendanceTime());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setAttendanceType(entity.getAttendanceType());
        return dto;
    }
}
