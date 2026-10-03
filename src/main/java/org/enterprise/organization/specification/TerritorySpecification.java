package org.enterprise.organization.specification;

import org.enterprise.organization.entity.Territory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class TerritorySpecification {

    private TerritorySpecification() {}

    public static Specification<Territory> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("code")), likePattern)
                );
            }
            return predicates;
        };
    }
}
