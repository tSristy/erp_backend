package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.security.dto.PermissionDto;
import org.enterprise.security.dto.RoleDto;
import org.enterprise.security.dto.RoleRequest;
import org.enterprise.security.entity.Permission;
import org.enterprise.security.entity.Role;
import org.enterprise.security.entity.RolePermission;
import org.enterprise.security.repository.PermissionRepository;
import org.enterprise.security.repository.RolePermissionRepository;
import org.enterprise.security.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public List<RoleDto> getAllRoles() {
        Long companyId = TenantContext.get().getCompanyId();
        return roleRepository.findAll().stream()
                .filter(r -> r.getCompanyId() == null || r.getCompanyId().equals(companyId))
                .map(this::mapToDto)
                .toList();
    }

    public RoleDto getRoleById(Long id) {
        return roleRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }



    public List<String> getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
                
        return role.getRolePermissions().stream()
                .filter(rp -> Boolean.TRUE.equals(rp.getAllowed()))
                .map(rp -> rp.getPermission().getCode())
                .toList();
    }

    @Transactional
    public RoleDto createRole(RoleRequest request) {
        Long companyId = TenantContext.get().getCompanyId();

        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setCompanyId(companyId);
        
        role = roleRepository.save(role);

        Set<RolePermission> permissions = new HashSet<>();
        if (request.getPermissions() != null) {
            for (String pCode : request.getPermissions()) {
                Permission perm = permissionRepository.findByCode(pCode)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + pCode));
                
                RolePermission rp = new RolePermission();
                rp.setRole(role);
                rp.setPermission(perm);
                rp.setCompanyId(companyId);
                rp.setAllowed(true);
                
                permissions.add(rolePermissionRepository.save(rp));
            }
        }
        role.setRolePermissions(permissions);
        
        return mapToDto(roleRepository.save(role));
    }

    @Transactional
    public RoleDto updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
                
        role.setCode(request.getCode());
        role.setName(request.getName());
        
        // Clear old permissions for simplicity
        if (role.getRolePermissions() != null) {
            rolePermissionRepository.deleteAll(role.getRolePermissions());
            role.getRolePermissions().clear();
        } else {
            role.setRolePermissions(new HashSet<>());
        }
        
        Long companyId = TenantContext.get().getCompanyId();
        
        if (request.getPermissions() != null) {
            for (String pCode : request.getPermissions()) {
                Permission perm = permissionRepository.findByCode(pCode)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + pCode));
                
                RolePermission rp = new RolePermission();
                rp.setRole(role);
                rp.setPermission(perm);
                rp.setCompanyId(companyId);
                rp.setAllowed(true);
                
                role.getRolePermissions().add(rolePermissionRepository.save(rp));
            }
        }
        
        return mapToDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    private RoleDto mapToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setCompanyId(role.getCompanyId());
        return dto;
    }
}
