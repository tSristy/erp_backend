package org.enterprise.security.controller;

import lombok.RequiredArgsConstructor;
import org.enterprise.security.entity.Module;
import org.enterprise.security.repository.ModuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.enterprise.security.repository.MenuRepository;
import org.enterprise.security.dto.MenuDTO;
import org.enterprise.common.util.TenantContext;
import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final MenuRepository menuRepository;

    @GetMapping
    public ResponseEntity<?> getAllModules() {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(401).body("Unauthorized: Context not found");
        }
        
        List<Module> modules = moduleRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getActive()) 
                          && Boolean.TRUE.equals(m.getInstalled())
                          && Boolean.TRUE.equals(m.getVisibleInLauncher())
                          && companyId.equals(m.getCompanyId()))
                .sorted((m1, m2) -> {
                    Integer order1 = m1.getDisplayOrder() != null ? m1.getDisplayOrder() : 999;
                    Integer order2 = m2.getDisplayOrder() != null ? m2.getDisplayOrder() : 999;
                    return order1.compareTo(order2);
                })
                .toList();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/{route}/menus")
    public ResponseEntity<?> getModuleMenus(@PathVariable String route) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            return ResponseEntity.status(401).body("Unauthorized: Context not found");
        }
        
        List<MenuDTO> menus = menuRepository.findByModuleRouteAndCompanyIdAndVisibleTrueOrderByDisplayOrderAsc(route, companyId)
                .stream()
                .map(menu -> {
                    MenuDTO dto = new MenuDTO();
                    dto.setId(menu.getId());
                    dto.setCode(menu.getCode());
                    dto.setName(menu.getName());
                    dto.setPath(menu.getPath());
                    dto.setIcon(menu.getIcon());
                    dto.setDisplayOrder(menu.getDisplayOrder());
                    if (menu.getParent() != null) {
                        dto.setParentId(menu.getParent().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(menus);
    }
}
