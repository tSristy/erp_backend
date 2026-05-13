package org.enterprise.crm.sales.dto;

import lombok.Data;
import org.enterprise.crm.sales.entity.Lead;

@Data
public class LeadDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String source;
    private Lead.LeadStatus status;
}
