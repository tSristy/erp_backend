package org.enterprise.sales.specification;

import org.enterprise.sales.entity.SalesInvoice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SalesInvoiceSpecification {

    private SalesInvoiceSpecification() {}

    public static Specification<SalesInvoice> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("invoiceNo")), likePattern)
                );
            }
            return predicates;
        };
    }
}
