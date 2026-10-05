package org.enterprise.inventory.specification;

import org.enterprise.inventory.entity.CostHead;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CostHeadSpecification {

    public static Specification<CostHead> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            // Id search removed because PostgreSQL doesn't support lower(bigint)
            try { predicates.add(cb.like(cb.lower(root.get("code")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("name")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("description")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("transactionNo")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("documentNo")), likePattern)); } catch (Exception e) {}
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
