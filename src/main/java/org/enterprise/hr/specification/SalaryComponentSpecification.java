package org.enterprise.hr.specification;

import org.enterprise.hr.entity.SalaryComponent;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SalaryComponentSpecification {

    public static Specification<SalaryComponent> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try {
            // Id search removed because PostgreSQL doesn't support lower(bigint)
            } catch (Exception e) {
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("code")), likePattern));
            } catch (Exception e) {
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            } catch (Exception e) {
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("employeeName")), likePattern));
            } catch (Exception e) {
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("employeeCode")), likePattern));
            } catch (Exception e) {
            }
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
