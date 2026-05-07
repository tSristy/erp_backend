package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "product_prices",
        indexes = {
                @Index(name = "idx_price_product", columnList = "product_id")
        }
)
@Getter
@Setter
public class ProductPrice extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    private PriceType priceType;

    private String currency;

    private Double price;

    private LocalDate validFrom;
    private LocalDate validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    public enum PriceType {
        COST, RETAIL, WHOLESALE
    }
}