package org.enterprise.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private Long id;

    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;

    private String gender;
    private String maritalStatus;

    private LocalDate dateOfBirth;
    private LocalDate joiningDate;

    private String mobile;
    private String email;

    private String presentAddress;
    private String permanentAddress;

    private Boolean active;

    private Long companyId;
    private String companyName;

    private Long branchId;
    private String branchName;

    private Long departmentId;
    private String departmentName;

    private Long designationId;
    private String designationName;

    private Long defaultShiftId;
    private String defaultShiftName;
}