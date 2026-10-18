package org.enterprise.inventory.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.enterprise.inventory.entity.LetterOfCredit;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LetterOfCreditSpecification {

    public static Specification<LetterOfCredit> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }

            String trimmed = query.trim().toLowerCase();
            String likePattern = "%" + trimmed + "%";
            List<Predicate> predicates = new ArrayList<>();

            try { predicates.add(cb.like(cb.lower(root.get("lcNumber")), likePattern)); } catch (Exception ignored) {}
            try { predicates.add(cb.like(cb.lower(root.get("advisingBank")), likePattern)); } catch (Exception ignored) {}
            try { predicates.add(cb.like(cb.lower(root.get("portOfLoading")), likePattern)); } catch (Exception ignored) {}
            try { predicates.add(cb.like(cb.lower(root.get("portOfDischarge")), likePattern)); } catch (Exception ignored) {}
            try { predicates.add(cb.like(cb.lower(root.get("currency")), likePattern)); } catch (Exception ignored) {}
            try { predicates.add(cb.like(cb.lower(root.get("remarks")), likePattern)); } catch (Exception ignored) {}

            try {
                Join<?, ?> bankJoin = root.join("issuingBank", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(bankJoin.get("bankName")), likePattern));
                predicates.add(cb.like(cb.lower(bankJoin.get("accountName")), likePattern));
                predicates.add(cb.like(cb.lower(bankJoin.get("accountNumber")), likePattern));
            } catch (Exception ignored) {}

            for (LetterOfCredit.LcStatus status : LetterOfCredit.LcStatus.values()) {
                if (status.name().toLowerCase().contains(trimmed)) {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
