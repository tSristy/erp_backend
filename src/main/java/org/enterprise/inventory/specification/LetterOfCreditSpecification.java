package org.enterprise.inventory.specification;

import org.enterprise.inventory.entity.LetterOfCredit;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class LetterOfCreditSpecification {

    public static Specification<LetterOfCredit> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }
            
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            try { predicates.add(cb.like(cb.lower(root.get("lcNumber")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("advisingBank")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("portOfLoading")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("portOfDischarge")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("currency")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.get("remarks")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("issuingBank", jakarta.persistence.criteria.JoinType.LEFT).get("bankName")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("issuingBank", jakarta.persistence.criteria.JoinType.LEFT).get("accountName")), likePattern)); } catch (Exception e) {}
            try { predicates.add(cb.like(cb.lower(root.join("issuingBank", jakarta.persistence.criteria.JoinType.LEFT).get("accountNumber")), likePattern)); } catch (Exception e) {}
            
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
