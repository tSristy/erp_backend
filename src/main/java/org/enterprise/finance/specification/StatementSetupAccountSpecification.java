package org.enterprise.finance.specification;

import org.enterprise.finance.entity.StatementSetupAccount;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class StatementSetupAccountSpecification {

    public static Specification<StatementSetupAccount> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try {
            // Id search removed because PostgreSQL doesn't support lower(bigint)
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("code")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("description")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
