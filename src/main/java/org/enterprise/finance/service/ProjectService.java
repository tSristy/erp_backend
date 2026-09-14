package org.enterprise.finance.service;

import lombok.RequiredArgsConstructor;
import org.enterprise.common.util.TenantContext;
import org.enterprise.finance.entity.Project;
import org.enterprise.finance.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public Page<Project> searchProjects(String search, int page, int size) {
        Long companyId = TenantContext.getCompanyId();
        Pageable pageable = PageRequest.of(page, size);
        return projectRepository.searchByCompanyId(companyId, search == null ? "" : search, pageable);
    }

    public Project getProjectById(Long id) {
        Long companyId = TenantContext.getCompanyId();
        return projectRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    @Transactional
    public Project createProject(Project project) {
        Long companyId = TenantContext.getCompanyId();
        
        if (project.getCode() == null || project.getCode().trim().isEmpty()) {
            String maxCode = projectRepository.findMaxCodeByCompanyId(companyId).orElse(null);
            int nextNum = 1;
            if (maxCode != null && maxCode.matches("^PRJ-\\d{6}$")) {
                try {
                    nextNum = Integer.parseInt(maxCode.substring(4)) + 1;
                } catch (NumberFormatException ignored) {}
            }
            project.setCode(String.format("PRJ-%06d", nextNum));
        } else {
            if (!project.getCode().matches("^[A-Z0-9]+-\\d{6}$")) {
                throw new RuntimeException("Project code must have a prefix followed by 6 digits (e.g., PRJ-000001)");
            }
            projectRepository.findByCodeAndCompanyId(project.getCode(), companyId)
                    .ifPresent(p -> {
                        throw new RuntimeException("Project code already exists: " + project.getCode());
                    });
        }
        
        project.setCompanyId(companyId);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, Project details) {
        Project project = getProjectById(id);
        
        if (details.getCode() != null && !details.getCode().equals(project.getCode())) {
            if (!details.getCode().matches("^[A-Z0-9]+-\\d{6}$")) {
                throw new RuntimeException("Project code must have a prefix followed by 6 digits (e.g., PRJ-000001)");
            }
            projectRepository.findByCodeAndCompanyId(details.getCode(), TenantContext.getCompanyId())
                    .ifPresent(p -> {
                        throw new RuntimeException("Project code already exists: " + details.getCode());
                    });
            project.setCode(details.getCode());
        }

        project.setName(details.getName());
        project.setStartDate(details.getStartDate());
        project.setEndDate(details.getEndDate());
        project.setBudget(details.getBudget());
        if (details.getActive() != null) {
            project.setActive(details.getActive());
        }

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }
}
