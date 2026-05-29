package org.enterprise.hr.service;

import org.enterprise.hr.dto.HolidayDto;
import org.enterprise.hr.entity.Holiday;
import org.enterprise.hr.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HolidayService {

    private final HolidayRepository repository;

    public HolidayDto create(HolidayDto dto) {
        Holiday entity = new Holiday();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public HolidayDto update(Long id, HolidayDto dto) {
        Holiday entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public HolidayDto getById(Long id) {
        Holiday entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Holiday not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<HolidayDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(HolidayDto dto, Holiday entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setFromDate(dto.getFromDate());
        entity.setToDate(dto.getToDate());
        entity.setType(dto.getType());
    }

    private HolidayDto mapEntityToDto(Holiday entity) {
        HolidayDto dto = new HolidayDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());
        dto.setType(entity.getType());
        return dto;
    }
}
