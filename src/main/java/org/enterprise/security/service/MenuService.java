package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.security.dto.MenuDTO;
import org.enterprise.security.entity.Menu;
import org.enterprise.security.entity.Module;
import org.enterprise.security.repository.MenuRepository;
import org.enterprise.security.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    public List<MenuDTO> findAll(String moduleCode) {
        Long companyId = TenantContext.getCompanyId();
        
        List<Menu> menus;
        if (moduleCode != null && !moduleCode.isEmpty()) {
            menus = menuRepository.findByModuleCodeAndCompanyIdOrderByDisplayOrderAsc(moduleCode, companyId);
        } else {
            menus = menuRepository.findAll().stream()
                    .filter(m -> companyId != null && companyId.equals(m.getCompanyId()))
                    .sorted((m1, m2) -> {
                        Integer o1 = m1.getDisplayOrder() != null ? m1.getDisplayOrder() : 999;
                        Integer o2 = m2.getDisplayOrder() != null ? m2.getDisplayOrder() : 999;
                        return o1.compareTo(o2);
                    })
                    .collect(Collectors.toList());
        }
        return menus.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuDTO> findMenusByModuleRoute(String route) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            return List.of();
        }
        return menuRepository.findByModuleRouteAndCompanyIdAndVisibleTrueOrderByDisplayOrderAsc(route, companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuDTO findById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        return menuRepository.findById(id)
                .filter(m -> companyId != null && companyId.equals(m.getCompanyId()))
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public MenuDTO save(MenuDTO dto) {
        Long companyId = TenantContext.getCompanyId();
        Menu entity = null;

        if (dto.getId() != null) {
            entity = menuRepository.findById(dto.getId()).orElse(null);
        }

        if (entity == null) {
            entity = new Menu();
            entity.setCompanyId(companyId);
        }

        toEntity(dto, entity);
        entity = menuRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void delete(Long id) {
        Long companyId = TenantContext.getCompanyId();
        menuRepository.findById(id).ifPresent(entity -> {
            if (companyId != null && companyId.equals(entity.getCompanyId())) {
                menuRepository.delete(entity);
            }
        });
    }

    private MenuDTO toDto(Menu entity) {
        if (entity == null) return null;
        MenuDTO dto = new MenuDTO();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompanyId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setPath(entity.getPath());
        dto.setIcon(entity.getIcon());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setVisible(entity.getVisible());
        dto.setIsReportMenu(entity.getIsReportMenu());

        if (entity.getParent() != null) {
            dto.setParentId(entity.getParent().getId());
            dto.setParent(entity.getParent().getCode());
        }

        if (entity.getModule() != null) {
            dto.setModuleId(entity.getModule().getId());
            dto.setModule(entity.getModule().getCode());
        }

        return dto;
    }

    private void toEntity(MenuDTO dto, Menu entity) {
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getPath() != null) entity.setPath(dto.getPath());
        if (dto.getIcon() != null) entity.setIcon(dto.getIcon());
        if (dto.getDisplayOrder() != null) entity.setDisplayOrder(dto.getDisplayOrder());
        if (dto.getVisible() != null) entity.setVisible(dto.getVisible());
        if (dto.getIsReportMenu() != null) entity.setIsReportMenu(dto.getIsReportMenu());

        if (dto.getParentId() != null) {
            Menu parent = menuRepository.findById(dto.getParentId()).orElse(null);
            entity.setParent(parent);
        } else if (dto.getParentId() == null && entity.getParent() != null) {
            // Unset parent if explicit null
        } // Simple logic for now

        if (dto.getModuleId() != null) {
            Module module = moduleRepository.findById(dto.getModuleId()).orElse(null);
            entity.setModule(module);
        }
    }
}
