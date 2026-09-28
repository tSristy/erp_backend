package org.enterprise.common.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.enterprise.security.repository.MenuRepository;
import org.enterprise.security.repository.ModuleRepository;
import org.enterprise.security.entity.Module;
import org.enterprise.security.entity.Menu;

@Component
@RequiredArgsConstructor
public class PatchRunner implements CommandLineRunner {

    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public void run(String... args) {
        Long companyId = 1L; // Assuming cid is 1
        
        moduleRepository.findByCodeAndCompanyId("ORGANIZATIONS", companyId).ifPresent(module -> {
            createMenu(module, "Zones", "organizations/zone", "Map", 6, companyId);
            createMenu(module, "Areas", "organizations/area", "MapPin", 7, companyId);
            createMenu(module, "Territories", "organizations/territory", "Navigation", 8, companyId);
        });
        
        System.out.println("PATCH RUNNER: Successfully inserted missing menus.");
    }

    private void createMenu(Module module, String name, String path, String icon, int order, Long companyId) {
        String code = module.getCode() + "_" + name.toUpperCase().replace(" ", "_");
        if (menuRepository.findByCodeAndCompanyId(code, companyId).isPresent()) return;
        
        Menu menu = new Menu();
        menu.setCode(code);
        menu.setName(name);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setDisplayOrder(order);
        menu.setVisible(true);
        menu.setModule(module);
        menu.setCompanyId(companyId);
        menuRepository.save(menu);
    }
}
