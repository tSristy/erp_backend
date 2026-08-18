package org.enterprise.reportengine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.reportengine.enums.ReportParamType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "report_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties("reportMaster")
public class ReportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String paramName;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportParamType paramType;

    @Lob
    private String queryList;

    @Column(length = 500)
    private String helpText;

    @Column(length = 500)
    private String defaultValue;

    @Column(length = 200)
    private String htmlClass;

    @Lob
    private String jsText;

    @Column(nullable = false)
    private Integer sortBy = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isMandatory = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 100)
    private String dependedElement;

    @Column(length = 100)
    private String placeholder;

    @Builder.Default
    @Column(nullable = false)
    private Boolean multiple = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_master_id", nullable = false)
    private ReportMaster reportMaster;
}

