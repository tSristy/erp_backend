package org.enterprise.security.repository;

import org.enterprise.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔐 Login lookup (tenant-aware recommended)
    Optional<User> findByUsername(String username);

    // 🏢 Tenant-based user fetch (important for admin screens)
    List<User> findByCompanyId(Long companyId);

    // 🔐 Active users only
    List<User> findByCompanyIdAndActiveTrue(Long companyId);

    // 🚀 Used for authentication optimization (join fetch role)
    // 🚀 Corrected query to match the 'roles' field in User entity
    @Query("""
    SELECT u FROM User u
    JOIN FETCH u.roles ur
    WHERE u.username = :username
""")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    // 🔥 Optional: tenant + username safety (recommended for SaaS)
    @Query("""
        SELECT u FROM User u
        WHERE u.username = :username
        AND u.companyId = :companyId
    """)
    Optional<User> findByUsernameAndCompanyId(
            @Param("username") String username,
            @Param("companyId") Long companyId
    );
}