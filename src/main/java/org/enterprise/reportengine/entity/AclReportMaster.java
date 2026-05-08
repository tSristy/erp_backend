package org.enterprise.reportengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "acl_report_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AclReportMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String rptGroup;

    @Column(length = 500)
    private String remarks;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Lob
    @Column(nullable = false)
    private String sqlQuery;

    @Lob
    private String columnHeader;

    @Column(nullable = false)
    private Integer sortBy = 0;

    @OneToMany(mappedBy = "aclReportMaster",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortBy ASC")
    @Builder.Default
    private List<AclReportDetail> parameters = new ArrayList<>();
}

