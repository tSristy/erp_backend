package org.enterprise.workflow.specification;

import org.enterprise.workflow.entity.WorkflowDefinition;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class WorkflowDefinitionSpecification {

    private WorkflowDefinitionSpecification() {}

    public static Specification<WorkflowDefinition> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("code")), likePattern),
                        cb.like(cb.lower(root.get("module")), likePattern),
                        cb.like(cb.lower(root.get("entityName")), likePattern)
                );
            }
            return predicates;
        };
    }
}
