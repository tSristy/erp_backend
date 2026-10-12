package org.enterprise.sales.specification;

import org.enterprise.sales.entity.SalesInvoice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SalesInvoiceSpecification {

    private SalesInvoiceSpecification() {}

    public static Specification<SalesInvoice> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            var customerJoin = root.join("customer", jakarta.persistence.criteria.JoinType.LEFT);
            var warehouseJoin = root.join("warehouse", jakarta.persistence.criteria.JoinType.LEFT);
            var deliveryNoteJoin = root.join("deliveryNote", jakarta.persistence.criteria.JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("invoiceNo")), likePattern),
                    cb.like(cb.lower(customerJoin.get("name")), likePattern),
                    cb.like(cb.lower(warehouseJoin.get("name")), likePattern),
                    cb.like(cb.lower(deliveryNoteJoin.get("deliveryNo")), likePattern)
            );
        };
    }
}
