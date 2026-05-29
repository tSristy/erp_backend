package org.enterprise.hr.service;

import org.enterprise.hr.dto.ShiftDto;
import org.enterprise.hr.entity.Shift;
import org.enterprise.hr.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftService {

    private final ShiftRepository repository;

    public ShiftDto create(ShiftDto dto) {
        Shift entity = new Shift();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public ShiftDto update(Long id, ShiftDto dto) {
        Shift entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public ShiftDto getById(Long id) {
        Shift entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<ShiftDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(ShiftDto dto, Shift entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setInTime(dto.getInTime());
        entity.setOutTime(dto.getOutTime());
        entity.setGraceMinutes(dto.getGraceMinutes());
    }

    private ShiftDto mapEntityToDto(Shift entity) {
        ShiftDto dto = new ShiftDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setInTime(entity.getInTime());
        dto.setOutTime(entity.getOutTime());
        dto.setGraceMinutes(entity.getGraceMinutes());
        return dto;
    }
}
