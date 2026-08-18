package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.security.dto.ModuleDto;
import org.enterprise.security.entity.Module;
import org.enterprise.security.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    public List<ModuleDto> findAll() {
        Long companyId = TenantContext.getCompanyId();
        return moduleRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getActive()) 
                          && Boolean.TRUE.equals(m.getInstalled())
                          && Boolean.TRUE.equals(m.getVisibleInLauncher())
                          && companyId != null && companyId.equals(m.getCompanyId()))
                .sorted((m1, m2) -> {
                    Integer order1 = m1.getDisplayOrder() != null ? m1.getDisplayOrder() : 999;
                    Integer order2 = m2.getDisplayOrder() != null ? m2.getDisplayOrder() : 999;
                    return order1.compareTo(order2);
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ModuleDto findById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        return moduleRepository.findById(id)
                .filter(m -> companyId != null && companyId.equals(m.getCompanyId()))
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public ModuleDto save(ModuleDto dto) {
        Long companyId = TenantContext.getCompanyId();
        Module entity = null;

        if (dto.getId() != null) {
            entity = moduleRepository.findById(dto.getId()).orElse(null);
        }

        if (entity == null) {
            entity = new Module();
            entity.setCompanyId(companyId);
        }

        toEntity(dto, entity);
        entity = moduleRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void delete(Long id) {
        Long companyId = TenantContext.getCompanyId();
        moduleRepository.findById(id).ifPresent(entity -> {
            if (companyId != null && companyId.equals(entity.getCompanyId())) {
                moduleRepository.delete(entity);
            }
        });
    }

    private ModuleDto toDto(Module entity) {
        if (entity == null) return null;
        ModuleDto dto = new ModuleDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setRoute(entity.getRoute());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setActive(entity.getActive());
        dto.setInstalled(entity.getInstalled());
        dto.setVisibleInLauncher(entity.getVisibleInLauncher());
        dto.setCategory(entity.getCategory());
        return dto;
    }

    private void toEntity(ModuleDto dto, Module entity) {
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getIcon() != null) entity.setIcon(dto.getIcon());
        if (dto.getRoute() != null) entity.setRoute(dto.getRoute());
        if (dto.getDisplayOrder() != null) entity.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        if (dto.getInstalled() != null) entity.setInstalled(dto.getInstalled());
        if (dto.getVisibleInLauncher() != null) entity.setVisibleInLauncher(dto.getVisibleInLauncher());
        if (dto.getCategory() != null) entity.setCategory(dto.getCategory());
    }
}
