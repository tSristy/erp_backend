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
        
        if (project.getCode() != null) {
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
