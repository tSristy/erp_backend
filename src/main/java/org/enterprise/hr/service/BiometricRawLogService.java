package org.enterprise.hr.service;

import org.enterprise.hr.dto.BiometricRawLogDto;
import org.enterprise.hr.entity.BiometricRawLog;
import org.enterprise.hr.repository.BiometricRawLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BiometricRawLogService {

    private final BiometricRawLogRepository repository;

    public BiometricRawLogDto create(BiometricRawLogDto dto) {
        BiometricRawLog entity = new BiometricRawLog();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public BiometricRawLogDto update(Long id, BiometricRawLogDto dto) {
        BiometricRawLog entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("BiometricRawLog not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public BiometricRawLogDto getById(Long id) {
        BiometricRawLog entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("BiometricRawLog not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<BiometricRawLogDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(BiometricRawLogDto dto, BiometricRawLog entity) {
        entity.setBiometricId(dto.getBiometricId());
        entity.setLogTime(dto.getLogTime());
        entity.setPunchType(dto.getPunchType());
    }

    private BiometricRawLogDto mapEntityToDto(BiometricRawLog entity) {
        BiometricRawLogDto dto = new BiometricRawLogDto();
        dto.setId(entity.getId());
        dto.setBiometricId(entity.getBiometricId());
        dto.setLogTime(entity.getLogTime());
        dto.setPunchType(entity.getPunchType());
        return dto;
    }
}
