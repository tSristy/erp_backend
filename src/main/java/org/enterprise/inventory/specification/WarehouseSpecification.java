package org.enterprise.inventory.specification;

import org.enterprise.inventory.entity.Warehouse;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class WarehouseSpecification {

    private WarehouseSpecification() {
        // Private constructor to hide the implicit public one
    }

    public static Specification<Warehouse> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("code")), likePattern),
                        cb.like(cb.lower(root.get("shortName")), likePattern)
                );
            }
            return predicates;
        };
    }
}
