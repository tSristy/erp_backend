package org.enterprise.hr.service;

import org.enterprise.hr.dto.BiometricDeviceDto;
import org.enterprise.hr.entity.BiometricDevice;
import org.enterprise.hr.repository.BiometricDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BiometricDeviceService {

    private final BiometricDeviceRepository repository;

    public BiometricDeviceDto create(BiometricDeviceDto dto) {
        BiometricDevice entity = new BiometricDevice();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public BiometricDeviceDto update(Long id, BiometricDeviceDto dto) {
        BiometricDevice entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("BiometricDevice not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public BiometricDeviceDto getById(Long id) {
        BiometricDevice entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("BiometricDevice not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<BiometricDeviceDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(BiometricDeviceDto dto, BiometricDevice entity) {
        entity.setDeviceCode(dto.getDeviceCode());
        entity.setDeviceName(dto.getDeviceName());
        entity.setIpAddress(dto.getIpAddress());
        entity.setPort(dto.getPort());
    }

    private BiometricDeviceDto mapEntityToDto(BiometricDevice entity) {
        BiometricDeviceDto dto = new BiometricDeviceDto();
        dto.setId(entity.getId());
        dto.setDeviceCode(entity.getDeviceCode());
        dto.setDeviceName(entity.getDeviceName());
        dto.setIpAddress(entity.getIpAddress());
        dto.setPort(entity.getPort());
        return dto;
    }
}
