package org.enterprise.security.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleRequest {
    private String code;
    private String name;
    private List<String> permissions;
}
