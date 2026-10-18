package org.enterprise.inventory.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.enterprise.inventory.entity.LandedCostVoucher;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class LandedCostVoucherSpecification {

    public static Specification<LandedCostVoucher> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.trim().isEmpty()) {
                return cb.conjunction();
            }

            String trimmed = query.trim().toLowerCase();
            String likePattern = "%" + trimmed + "%";
            List<Predicate> predicates = new ArrayList<>();

            try { predicates.add(cb.like(cb.lower(root.get("voucherNo")), likePattern)); } catch (Exception ignored) {}
            try {
                Join<?, ?> grnJoin = root.join("goodsReceipt", JoinType.LEFT);
                predicates.add(cb.like(cb.lower(grnJoin.get("grnNo")), likePattern));
            } catch (Exception ignored) {}

            for (LandedCostVoucher.LandedCostStatus status : LandedCostVoucher.LandedCostStatus.values()) {
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
