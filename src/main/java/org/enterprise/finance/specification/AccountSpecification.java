package org.enterprise.finance.specification;

import org.enterprise.finance.entity.Account;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AccountSpecification {

    public static Specification<Account> searchByQuery(String query) {
        return (root, cq, cb) -> {
            var predicates = cb.conjunction();
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                predicates = cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("code")), likePattern)
                );
            }
            return predicates;
        };
    }
}
