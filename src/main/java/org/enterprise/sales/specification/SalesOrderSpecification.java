package org.enterprise.sales.specification;

import org.enterprise.sales.entity.SalesOrder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SalesOrderSpecification {

    private SalesOrderSpecification() {}

    public static Specification<SalesOrder> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }
            String likePattern = "%" + query.trim().toLowerCase() + "%";
            var customerJoin = root.join("customer", jakarta.persistence.criteria.JoinType.LEFT);
            var warehouseJoin = root.join("warehouse", jakarta.persistence.criteria.JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("orderNo")), likePattern),
                    cb.like(cb.lower(customerJoin.get("name")), likePattern),
                    cb.like(cb.lower(warehouseJoin.get("name")), likePattern)
            );
        };
    }
}
