package org.enterprise.security.dto;

import lombok.Data;

@Data
public class MenuDTO {
    private Long id;
    private String code;
    private String name;
    private String path;
    private String icon;
    private Integer displayOrder;
    private Long parentId;
}
