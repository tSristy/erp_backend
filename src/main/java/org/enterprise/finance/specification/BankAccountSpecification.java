package org.enterprise.finance.specification;

import org.enterprise.finance.entity.BankAccount;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BankAccountSpecification {

    public static Specification<BankAccount> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try {
                predicates.add(cb.like(cb.lower(root.get("accountName")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("bankName")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("branchName")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("accountNumber")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("routingNumber")), likePattern));
            } catch (Exception e) {
                // Ignore
            }
            try {
                predicates.add(cb.like(cb.lower(root.get("currencyCode")), likePattern));
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
