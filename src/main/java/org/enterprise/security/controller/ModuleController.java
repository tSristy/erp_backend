package org.enterprise.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.security.dto.ModuleDto;
import org.enterprise.security.dto.MenuDTO;
import org.enterprise.security.service.ModuleService;
import org.enterprise.security.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/security/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;
    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<ModuleDto>> getAllModules() {
        return ResponseEntity.ok(moduleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleDto> getModuleById(@PathVariable Long id) {
        ModuleDto dto = moduleService.findById(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ModuleDto> createModule(@Valid @RequestBody ModuleDto dto) {
        ModuleDto created = moduleService.save(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleDto> updateModule(@PathVariable Long id, @Valid @RequestBody ModuleDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(moduleService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{route}/menus")
    public ResponseEntity<List<MenuDTO>> getModuleMenus(@PathVariable String route) {
        return ResponseEntity.ok(menuService.findMenusByModuleRoute(route));
    }
}
