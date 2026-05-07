package org.enterprise.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserContext {

    private Long userId;

    private Long companyId;

    private List<String> roles;

    private List<String> permissions;

    private List<Long> branchIds;

    private List<Long> warehouseIds;

    private List<Long> profitCenterIds;

    private List<Long> costCenterIds;

    private String timezone;

    private String language;
}