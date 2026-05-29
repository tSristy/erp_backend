package org.enterprise.hr.service;

import org.enterprise.hr.dto.WeekendDto;
import org.enterprise.hr.entity.Weekend;
import org.enterprise.hr.repository.WeekendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WeekendService {

    private final WeekendRepository repository;

    public WeekendDto create(WeekendDto dto) {
        Weekend entity = new Weekend();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public WeekendDto update(Long id, WeekendDto dto) {
        Weekend entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weekend not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public WeekendDto getById(Long id) {
        Weekend entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weekend not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<WeekendDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(WeekendDto dto, Weekend entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setWeekend1(dto.getWeekend1());
        entity.setWeekend2(dto.getWeekend2());
    }

    private WeekendDto mapEntityToDto(Weekend entity) {
        WeekendDto dto = new WeekendDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setWeekend1(entity.getWeekend1());
        dto.setWeekend2(entity.getWeekend2());
        return dto;
    }
}
