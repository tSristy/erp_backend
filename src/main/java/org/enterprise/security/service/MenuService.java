package org.enterprise.security.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.security.dto.MenuDTO;
import org.enterprise.security.entity.Menu;
import org.enterprise.security.entity.Module;
import org.enterprise.security.repository.MenuRepository;
import org.enterprise.security.repository.ModuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;

    @Transactional
    public Page<MenuDTO> search(String moduleCode, String query, Pageable pageable) {
        Long companyId = TenantContext.getCompanyId();
        ensurePosReportMenus(companyId);
        return menuRepository.search(companyId, moduleCode, query, pageable).map(this::toDto);
    }

    @Transactional
    public List<MenuDTO> findAll(String moduleCode) {
        Long companyId = TenantContext.getCompanyId();
        ensurePosReportMenus(companyId);
        
        List<Menu> menus;
        if (moduleCode != null && !moduleCode.isEmpty()) {
            menus = menuRepository.findByModuleCodeAndCompanyIdOrderByDisplayOrderAsc(moduleCode, companyId);
        } else {
            menus = menuRepository.findAll().stream()
                    .filter(m -> companyId != null && companyId.equals(m.getCompanyId()))
                    .sorted((m1, m2) -> {
                        Integer o1 = m1.getDisplayOrder() != null ? m1.getDisplayOrder() : 999;
                        Integer o2 = m2.getDisplayOrder() != null ? m1.getDisplayOrder() : 999;
                        return o1.compareTo(o2);
                    })
                    .collect(Collectors.toList());
        }
        return menus.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public List<MenuDTO> findMenusByModuleRoute(String route) {
        Long companyId = TenantContext.getCompanyId();
        if (companyId == null) {
            return List.of();
        }
        ensurePosReportMenus(companyId);
        return menuRepository.findByModuleRouteAndCompanyIdAndVisibleTrueOrderByDisplayOrderAsc(route, companyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void ensurePosReportMenus(Long companyId) {
        if (companyId == null) return;
        moduleRepository.findByCodeAndCompanyId("RETAIL_POS", companyId).ifPresent(retail -> {
            ensureMenu(retail, "Reports", "retail-pos/reports", "BarChart2", 4, companyId, false);
            ensureMenu(retail, "POS Sales Register", "retail-pos/reports/sales", "ShoppingCart", 5, companyId, true);
            ensureMenu(retail, "Product & SKU Sales Summary", "retail-pos/reports/products", "Package", 6, companyId, true);
            ensureMenu(retail, "Payment Tender Summary", "retail-pos/reports/payments", "CreditCard", 7, companyId, true);
            ensureMenu(retail, "Customer & Walk-in Analysis", "retail-pos/reports/customers", "Users", 8, companyId, true);
            ensureMenu(retail, "Outlet / Warehouse Sales Summary", "retail-pos/reports/warehouses", "Warehouse", 9, companyId, true);
            ensureMenu(retail, "POS Returns Register", "retail-pos/reports/returns", "Undo2", 10, companyId, true);
        });
        moduleRepository.findByCodeAndCompanyId("RESTAURANT_POS", companyId).ifPresent(restPos -> {
            ensureMenu(restPos, "Reports", "restaurant-pos/reports", "BarChart2", 8, companyId, false);
            ensureMenu(restPos, "Order & Billing Register", "restaurant-pos/reports/orders", "ClipboardList", 9, companyId, true);
            ensureMenu(restPos, "Menu Item Sales Summary", "restaurant-pos/reports/items", "Coffee", 10, companyId, true);
            ensureMenu(restPos, "Table & Service Mode Analysis", "restaurant-pos/reports/tables", "Grid", 11, companyId, true);
            ensureMenu(restPos, "Waiter Performance", "restaurant-pos/reports/waiters", "Users", 12, companyId, true);
            ensureMenu(restPos, "Payment Mode Settlement", "restaurant-pos/reports/payments", "CreditCard", 13, companyId, true);
            ensureMenu(restPos, "Returns & Cancellations", "restaurant-pos/reports/returns", "Undo2", 14, companyId, true);
        });
    }

    private void ensureMenu(Module module, String name, String path, String icon, int order, Long companyId, boolean isReport) {
        String code = module.getCode() + "_" + name.toUpperCase().replace(" ", "_");
        if (menuRepository.findByCodeAndCompanyId(code, companyId).isPresent()) return;
        Menu menu = new Menu();
        menu.setCode(code);
        menu.setName(name);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setDisplayOrder(order);
        menu.setVisible(true);
        menu.setIsReportMenu(isReport);
        menu.setModule(module);
        menu.setCompanyId(companyId);
        menuRepository.save(menu);
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
