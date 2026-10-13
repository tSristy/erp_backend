package org.enterprise.inventory.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.enterprise.inventory.entity.Warehouse;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WarehouseSpecification {

    private WarehouseSpecification() {
        // Private constructor to hide the implicit public one
    }

    public static Specification<Warehouse> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }

            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            try {
                predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("code")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("shortName")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("city")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("address")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("contactPerson")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("email")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("phone")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.join("branch", JoinType.LEFT).get("name")), likePattern));
            } catch (Exception ignored) {}

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
