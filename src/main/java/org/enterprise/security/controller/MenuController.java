package org.enterprise.security.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.enterprise.security.dto.MenuDTO;
import org.enterprise.security.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/security/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuDTO>> getAllMenus(@RequestParam(required = false) String moduleCode) {
        return ResponseEntity.ok(menuService.findAll(moduleCode));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MenuDTO>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String moduleCode,
            Pageable pageable) {
        String searchQuery = query != null ? query : q;
        return ResponseEntity.ok(menuService.search(moduleCode, searchQuery, pageable));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<MenuDTO> getMenuById(@PathVariable Long id) {
        MenuDTO dto = menuService.findById(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<MenuDTO> createMenu(@Valid @RequestBody MenuDTO dto) {
        MenuDTO created = menuService.save(dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuDTO> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(menuService.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
