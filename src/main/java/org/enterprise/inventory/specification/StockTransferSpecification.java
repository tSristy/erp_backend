package org.enterprise.inventory.specification;

import org.enterprise.inventory.entity.StockTransfer;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class StockTransferSpecification {

    public static Specification<StockTransfer> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try { predicates.add(cb.like(cb.lower(root.get("transferNo")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("sourceWarehouse", jakarta.persistence.criteria.JoinType.LEFT).get("name")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("sourceWarehouse", jakarta.persistence.criteria.JoinType.LEFT).get("code")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("destinationWarehouse", jakarta.persistence.criteria.JoinType.LEFT).get("name")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("destinationWarehouse", jakarta.persistence.criteria.JoinType.LEFT).get("code")), likePattern)); } catch (Exception e) {}
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
