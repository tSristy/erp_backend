package org.enterprise.inventory.specification;

import jakarta.persistence.criteria.JoinType;
import org.enterprise.inventory.entity.PurchaseOrder;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderSpecification {

    public static Specification<PurchaseOrder> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try { predicates.add(cb.like(cb.lower(root.get("poNo")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("remarks")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("vendor", JoinType.LEFT).get("name")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("warehouse", JoinType.LEFT).get("name")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("letterOfCredit", JoinType.LEFT).get("lcNumber")), likePattern)); } catch (Exception e) {}
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
