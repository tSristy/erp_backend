package org.enterprise.common.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enterprise.security.entity.Menu;
import org.enterprise.security.entity.Module;
import org.enterprise.security.repository.MenuRepository;
import org.enterprise.security.repository.ModuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import java.util.List;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class ReportMenuSeeder implements CommandLineRunner {

    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Running Report Menu Seeder...");
        List<Module> settingsModules = moduleRepository.findAll().stream()
                .filter(m -> "SETTINGS".equals(m.getCode()))
                .toList();
                
        for (Module settingsModule : settingsModules) {
            Long companyId = settingsModule.getCompanyId();
            
            boolean menuExists = menuRepository.findAll().stream()
                    .anyMatch(m -> "Report Master".equals(m.getName()) && m.getCompanyId().equals(companyId));
                    
            if (!menuExists) {
                Menu menu = new Menu();
                menu.setModule(settingsModule);
                menu.setName("Report Master");
                menu.setPath("settings/report-master");
                menu.setIcon("Database");
                menu.setDisplayOrder(3);
                menu.setCompanyId(companyId);
                menu.setIsReportMenu(false);
                menuRepository.save(menu);
                log.info("Created Report Master menu for company " + companyId);
            }
        }
    }
}
