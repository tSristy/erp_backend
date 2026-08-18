package org.enterprise.finance.service;

import org.enterprise.finance.dto.FiscalYearDTO;
import org.enterprise.finance.entity.FiscalYear;
import org.enterprise.finance.repository.FiscalYearRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.enterprise.common.util.TenantContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FiscalYearService {

    private final FiscalYearRepository fiscalYearRepository;

    public FiscalYearService(FiscalYearRepository fiscalYearRepository) {
        this.fiscalYearRepository = fiscalYearRepository;
    }

    @Transactional(readOnly = true)
    public List<FiscalYearDTO> findAll() {
        return fiscalYearRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FiscalYearDTO findById(Long id) {
        return fiscalYearRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public FiscalYearDTO save(FiscalYearDTO dto) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            throw new RuntimeException("No active company context");
        }

        FiscalYear entity = convertToEntity(dto);
        entity.setCompanyId(companyId);
        
        if (entity.getLines() != null) {
            for (org.enterprise.finance.entity.FiscalPeriod line : entity.getLines()) {
                line.setCompanyId(companyId);
            }
        }
        
        FiscalYear saved = fiscalYearRepository.save(entity);
        return convertToDTO(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        fiscalYearRepository.deleteById(id);
    }

    private FiscalYearDTO convertToDTO(FiscalYear entity) {
        FiscalYearDTO dto = new FiscalYearDTO();
        BeanUtils.copyProperties(entity, dto, "lines");
        if (entity.getLines() != null) {
            dto.setLines(entity.getLines().stream().map(line -> {
                org.enterprise.finance.dto.FiscalPeriodDTO lineDto = new org.enterprise.finance.dto.FiscalPeriodDTO();
                BeanUtils.copyProperties(line, lineDto);
                return lineDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private FiscalYear convertToEntity(FiscalYearDTO dto) {
        FiscalYear entity = new FiscalYear();
        BeanUtils.copyProperties(dto, entity, "lines");
        if (dto.getLines() != null) {
            entity.setLines(dto.getLines().stream().map(lineDto -> {
                org.enterprise.finance.entity.FiscalPeriod line = new org.enterprise.finance.entity.FiscalPeriod();
                BeanUtils.copyProperties(lineDto, line);
                line.setFiscalYear(entity);
                return line;
            }).collect(Collectors.toList()));
        }
        return entity;
    }
}
