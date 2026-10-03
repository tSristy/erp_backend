package org.enterprise.inventory.specification;

import org.enterprise.inventory.entity.BusinessPartner;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BusinessPartnerSpecification {

    public static Specification<BusinessPartner> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try { predicates.add(cb.like(cb.lower(root.get("id").as(String.class)), likePattern)); } catch (Exception e) {}
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
