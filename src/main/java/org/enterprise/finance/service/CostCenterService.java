package org.enterprise.finance.service;

import org.enterprise.finance.dto.CostCenterDTO;
import org.enterprise.finance.entity.CostCenter;
import org.enterprise.finance.repository.CostCenterRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;

    public CostCenterService(CostCenterRepository costCenterRepository) {
        this.costCenterRepository = costCenterRepository;
    }

    @Transactional(readOnly = true)
    public List<CostCenterDTO> findAll() {
        return costCenterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CostCenterDTO findById(Long id) {
        return costCenterRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public CostCenterDTO save(CostCenterDTO dto) {
        CostCenter entity = convertToEntity(dto);
        CostCenter saved = costCenterRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        costCenterRepository.deleteById(id);
    }

    private CostCenterDTO convertToDTO(CostCenter entity) {
        CostCenterDTO dto = new CostCenterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private CostCenter convertToEntity(CostCenterDTO dto) {
        CostCenter entity = new CostCenter();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
