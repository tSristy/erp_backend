package org.enterprise.security.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private String mobile;
    private Boolean active;
    private Boolean locked;
    private List<Long> roles;
    private List<Long> companies;
    private List<Long> branches;
    private List<Long> warehouses;
    private List<Long> profitCenters;
    private List<Long> costCenters;
}
