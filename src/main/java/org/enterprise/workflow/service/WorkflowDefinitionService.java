package org.enterprise.workflow.service;

import org.enterprise.inventory.service.BaseService;
import org.enterprise.workflow.entity.WorkflowDefinition;
import org.enterprise.workflow.repository.WorkflowDefinitionRepository;
import org.enterprise.workflow.specification.WorkflowDefinitionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowDefinitionService extends BaseService<WorkflowDefinition, Long> {

    private final WorkflowDefinitionRepository repository;

    public WorkflowDefinitionService(WorkflowDefinitionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<WorkflowDefinition> search(String query, Pageable pageable) {
        return repository.findAll(WorkflowDefinitionSpecification.searchByQuery(query), pageable);
    }
}
