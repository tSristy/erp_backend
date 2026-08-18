package org.enterprise.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Warehouse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sal_sales_quotations")
@Getter
@Setter
public class SalesQuotation extends AuditableEntity {

    private String quotationNo;

    private LocalDate quotationDate;
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    private QuotationStatus status = QuotationStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @Column(precision = 18, scale = 2)
    private BigDecimal subTotal = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "salesQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesQuotationDiscount> discounts;

    @OneToMany(mappedBy = "salesQuotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesQuotationDetail> details;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee salesPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee territoryManager;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee areaManager;

    @ManyToOne(fetch = FetchType.LAZY)
    private org.enterprise.hr.entity.Employee regionalManager;

    public enum QuotationStatus {
        DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED
    }

    public enum SalesChannel {
        B2B, B2C, RETAIL, ECOMMERCE
    }

    @Enumerated(EnumType.STRING)
    private SalesChannel salesChannel = SalesChannel.B2C;
}
