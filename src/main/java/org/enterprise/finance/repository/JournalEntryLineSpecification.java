package org.enterprise.finance.repository;

import jakarta.persistence.criteria.JoinType;
import org.enterprise.finance.entity.JournalEntryLine;
import org.springframework.data.jpa.domain.Specification;
import org.enterprise.finance.enums.JournalStatus;

import java.time.LocalDate;

public class JournalEntryLineSpecification {

    public static Specification<JournalEntryLine> byDimensionAndDateRange(String dimensionType, String dimensionCode, LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            var jeJoin = root.join("journalEntry", JoinType.INNER);

            // Only Posted Journals
            predicates.add(
                criteriaBuilder.equal(jeJoin.get("status"), JournalStatus.POSTED)
            );

            // Date Range
            if (startDate != null) {
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(jeJoin.get("postingDate"), startDate)
                );
            }
            if (endDate != null) {
                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(jeJoin.get("postingDate"), endDate)
                );
            }

            // Dimension Filter
            switch (dimensionType.toUpperCase()) {
                case "PROJECT":
                    predicates.add(criteriaBuilder.equal(root.join("project", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "LOAN":
                    predicates.add(criteriaBuilder.equal(root.join("loan", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "INTERNAL_ORDER":
                    predicates.add(criteriaBuilder.equal(root.join("internalOrder", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "LETTER_OF_CREDIT":
                    predicates.add(criteriaBuilder.equal(root.join("letterOfCredit", JoinType.LEFT).get("lcNumber"), dimensionCode));
                    break;
                case "EMPLOYEE":
                case "CUSTOMER":
                case "VENDOR":
                case "BUSINESS_PARTNER":
                    predicates.add(criteriaBuilder.equal(root.join("businessPartner", JoinType.LEFT).get("code"), dimensionCode));
                    if (!dimensionType.equalsIgnoreCase("BUSINESS_PARTNER")) {
                        predicates.add(
                            criteriaBuilder.equal(
                                root.join("businessPartner", JoinType.LEFT).join("roles", JoinType.INNER).get("role"),
                                org.enterprise.inventory.entity.BusinessPartnerRole.RoleType.valueOf(dimensionType.toUpperCase())
                            )
                        );
                    }
                    break;
                case "COST_CENTER":
                    predicates.add(criteriaBuilder.equal(root.join("costCenter", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "PROFIT_CENTER":
                    predicates.add(criteriaBuilder.equal(root.join("profitCenter", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported dimension type: " + dimensionType);
            }

            query.orderBy(
                criteriaBuilder.asc(jeJoin.get("postingDate")),
                criteriaBuilder.asc(root.get("id"))
            );

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<JournalEntryLine> calculateRollForward(String dimensionType, String dimensionCode, LocalDate beforeDate) {
        return (root, query, criteriaBuilder) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            var jeJoin = root.join("journalEntry", JoinType.INNER);

            predicates.add(
                criteriaBuilder.equal(jeJoin.get("status"), JournalStatus.POSTED)
            );

            if (beforeDate != null) {
                predicates.add(
                    criteriaBuilder.lessThan(jeJoin.get("postingDate"), beforeDate)
                );
            }

            switch (dimensionType.toUpperCase()) {
                case "PROJECT":
                    predicates.add(criteriaBuilder.equal(root.join("project", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "LOAN":
                    predicates.add(criteriaBuilder.equal(root.join("loan", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "INTERNAL_ORDER":
                    predicates.add(criteriaBuilder.equal(root.join("internalOrder", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "LETTER_OF_CREDIT":
                    predicates.add(criteriaBuilder.equal(root.join("letterOfCredit", JoinType.LEFT).get("lcNumber"), dimensionCode));
                    break;
                case "EMPLOYEE":
                case "CUSTOMER":
                case "VENDOR":
                case "BUSINESS_PARTNER":
                    predicates.add(criteriaBuilder.equal(root.join("businessPartner", JoinType.LEFT).get("code"), dimensionCode));
                    if (!dimensionType.equalsIgnoreCase("BUSINESS_PARTNER")) {
                        predicates.add(
                            criteriaBuilder.equal(
                                root.join("businessPartner", JoinType.LEFT).join("roles", JoinType.INNER).get("role"),
                                org.enterprise.inventory.entity.BusinessPartnerRole.RoleType.valueOf(dimensionType.toUpperCase())
                            )
                        );
                    }
                    break;
                case "COST_CENTER":
                    predicates.add(criteriaBuilder.equal(root.join("costCenter", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                case "PROFIT_CENTER":
                    predicates.add(criteriaBuilder.equal(root.join("profitCenter", JoinType.LEFT).get("code"), dimensionCode));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported dimension type: " + dimensionType);
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
