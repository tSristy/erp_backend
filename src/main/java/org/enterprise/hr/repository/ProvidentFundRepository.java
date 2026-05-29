package org.enterprise.hr.repository;

import org.enterprise.hr.entity.ProvidentFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvidentFundRepository extends JpaRepository<ProvidentFund, Long> {
}
