package org.enterprise.reportengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "report_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportMaster {

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

    @OneToMany(mappedBy = "reportMaster",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortBy ASC")
    @Builder.Default
    private List<ReportDetail> parameters = new ArrayList<>();

    public void setParameters(List<ReportDetail> parameters) {
        if (parameters != null) {
            this.parameters.removeIf(existing -> parameters.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ReportDetail item : parameters) {
                if (item.getId() == null || this.parameters.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setReportMaster(this);
                    this.parameters.add(item);
                } else {
                    ReportDetail existing = this.parameters.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "reportMaster");
                }
            }
        } else {
            this.parameters.clear();
        }
    }
}

