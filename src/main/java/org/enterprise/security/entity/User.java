package org.enterprise.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private Boolean active = true;

    private String email;

    private String mobile;

    private Boolean locked = false;

    private Integer failedLoginAttempts = 0;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserRole> roles;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserCompany> companies;

    @OneToMany(mappedBy = "user")
    private List<UserBranch> userBranches;

    @OneToMany(mappedBy = "user")
    private List<UserWarehouse> userWarehouses;

    @OneToMany(mappedBy = "user")
    private List<UserProfitCenter> userProfitCenters;

    @OneToMany(mappedBy = "user")
    private List<UserCostCenter> userCostCenters;
}