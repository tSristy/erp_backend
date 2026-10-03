package org.enterprise.sales.specification;

import org.enterprise.sales.entity.SalesQuotation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SalesQuotationSpecification {

    private SalesQuotationSpecification() {}

    public static Specification<SalesQuotation> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("quotationNo")), likePattern)
                );
            }
            return predicates;
        };
    }
}
