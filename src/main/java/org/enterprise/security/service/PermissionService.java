package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.security.dto.PermissionDto;
import org.enterprise.security.entity.Menu;
import org.enterprise.security.entity.Permission;
import org.enterprise.security.repository.MenuRepository;
import org.enterprise.security.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<PermissionDto> findAll() {
        return permissionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionDto findById(Long id) {
        return permissionRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public PermissionDto save(PermissionDto dto) {
        Permission entity = null;
        if (dto.getId() != null) {
            entity = permissionRepository.findById(dto.getId()).orElse(null);
        }
        if (entity == null) {
            entity = new Permission();
        }

        toEntity(dto, entity);
        entity = permissionRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void delete(Long id) {
        permissionRepository.findById(id).ifPresent(permissionRepository::delete);
    }

    private PermissionDto toDto(Permission entity) {
        if (entity == null) return null;
        PermissionDto dto = new PermissionDto();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setModuleCode(entity.getModuleCode());
        dto.setActionPath(entity.getActionPath());
        dto.setActionType(entity.getActionType());
        dto.setDescription(entity.getDescription());

        if (entity.getMenu() != null) {
            dto.setMenuId(entity.getMenu().getId());
            dto.setMenuCode(entity.getMenu().getCode());
        }

        return dto;
    }

    private void toEntity(PermissionDto dto, Permission entity) {
        if (dto.getCode() != null) entity.setCode(dto.getCode());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getModuleCode() != null) entity.setModuleCode(dto.getModuleCode());
        if (dto.getActionPath() != null) entity.setActionPath(dto.getActionPath());
        if (dto.getActionType() != null) entity.setActionType(dto.getActionType());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());

        if (dto.getMenuId() != null) {
            Menu menu = menuRepository.findById(dto.getMenuId()).orElse(null);
            entity.setMenu(menu);
        }
    }
}
