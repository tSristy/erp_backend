package org.enterprise.crm.specification;

import org.enterprise.crm.entity.LoyaltyProfile;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class LoyaltyProfileSpecification {

    public static Specification<LoyaltyProfile> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            // Assuming customer has a "name" or "code" field
            predicates.add(cb.like(cb.lower(root.get("customer").get("name")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("customer").get("code")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("loyaltyTier")), likePattern));
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
