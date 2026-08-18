package org.enterprise.security.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String mobile;
    private Boolean active;
    private Boolean locked;
    private Integer failedLoginAttempts;
    private java.util.List<Long> roles;
    private java.util.List<Long> companies;
    private java.util.List<Long> branches;
    private java.util.List<Long> warehouses;
    private java.util.List<Long> profitCenters;
    private java.util.List<Long> costCenters;
}
