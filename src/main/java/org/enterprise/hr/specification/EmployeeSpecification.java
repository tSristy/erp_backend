package org.enterprise.hr.specification;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.enterprise.hr.entity.Employee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {

    public static Specification<Employee> searchByQuery(String query) {
        return (root, cq, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }

            String likePattern = "%" + query.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            try {
                predicates.add(cb.like(cb.lower(root.get("employeeCode")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("fullName")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("firstName")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("lastName")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("email")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.get("mobile")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.join("department", JoinType.LEFT).get("name")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.join("designation", JoinType.LEFT).get("name")), likePattern));
            } catch (Exception ignored) {}
            try {
                predicates.add(cb.like(cb.lower(root.join("branch", JoinType.LEFT).get("name")), likePattern));
            } catch (Exception ignored) {}

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
