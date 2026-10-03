package org.enterprise.sales.specification;

import org.enterprise.sales.entity.DeliveryNote;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DeliveryNoteSpecification {

    private DeliveryNoteSpecification() {}

    public static Specification<DeliveryNote> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("deliveryNo")), likePattern)
                );
            }
            return predicates;
        };
    }
}
