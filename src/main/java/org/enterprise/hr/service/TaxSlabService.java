package org.enterprise.hr.service;

import org.enterprise.hr.dto.TaxSlabDto;
import org.enterprise.hr.entity.TaxSlab;
import org.enterprise.hr.repository.TaxSlabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaxSlabService {

    private final TaxSlabRepository repository;

    public TaxSlabDto create(TaxSlabDto dto) {
        TaxSlab entity = new TaxSlab();
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    public TaxSlabDto update(Long id, TaxSlabDto dto) {
        TaxSlab entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaxSlab not found"));
        mapDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public TaxSlabDto getById(Long id) {
        TaxSlab entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("TaxSlab not found"));
        return mapEntityToDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<TaxSlabDto> search(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapEntityToDto);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapDtoToEntity(TaxSlabDto dto, TaxSlab entity) {
        entity.setFromAmount(dto.getFromAmount());
        entity.setToAmount(dto.getToAmount());
        entity.setPercentage(dto.getPercentage());
    }

    private TaxSlabDto mapEntityToDto(TaxSlab entity) {
        TaxSlabDto dto = new TaxSlabDto();
        dto.setId(entity.getId());
        dto.setFromAmount(entity.getFromAmount());
        dto.setToAmount(entity.getToAmount());
        dto.setPercentage(entity.getPercentage());
        return dto;
    }
}
