package org.enterprise.finance.service;

import org.enterprise.finance.dto.ProfitCenterDTO;
import org.enterprise.finance.entity.ProfitCenter;
import org.enterprise.finance.repository.ProfitCenterRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.enterprise.common.util.TenantContext;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfitCenterService {

    private final ProfitCenterRepository profitCenterRepository;

    public ProfitCenterService(ProfitCenterRepository profitCenterRepository) {
        this.profitCenterRepository = profitCenterRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfitCenterDTO> findAll() {
        return profitCenterRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfitCenterDTO findById(Long id) {
        return profitCenterRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public ProfitCenterDTO save(ProfitCenterDTO dto) {
        ProfitCenter entity = convertToEntity(dto);
        
        Long companyId = TenantContext.getCompanyId();
        if (companyId != null) {
            entity.setCompanyId(companyId);
        }
        
        ProfitCenter saved = profitCenterRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        profitCenterRepository.deleteById(id);
    }

    private ProfitCenterDTO convertToDTO(ProfitCenter entity) {
        ProfitCenterDTO dto = new ProfitCenterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private ProfitCenter convertToEntity(ProfitCenterDTO dto) {
        ProfitCenter entity = new ProfitCenter();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}
