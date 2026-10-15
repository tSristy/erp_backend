package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.ProductVariant;

import java.math.BigDecimal;

@Entity
@Table(name = "crm_service_parts_req_details")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServicePartsRequisitionDetail extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "details"})
    private ServicePartsRequisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductVariant variant;

    @Column(precision = 18, scale = 2)
    private BigDecimal quantityRequested = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal quantityIssued = BigDecimal.ZERO;

    @Transient
    private Long transientProductId;

    @com.fasterxml.jackson.annotation.JsonProperty("productId")
    public Long getProductId() {
        return product != null ? product.getId() : transientProductId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("productId")
    public void setProductId(Long productId) {
        this.transientProductId = productId;
        if (productId != null) {
            if (this.product == null) {
                this.product = new Product();
            }
            this.product.setId(productId);
        }
    }

    @Transient
    private Long transientVariantId;

    @com.fasterxml.jackson.annotation.JsonProperty("variantId")
    public Long getVariantId() {
        return variant != null ? variant.getId() : transientVariantId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("variantId")
    public void setVariantId(Long variantId) {
        this.transientVariantId = variantId;
        if (variantId != null) {
            if (this.variant == null) {
                this.variant = new ProductVariant();
            }
            this.variant.setId(variantId);
        }
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("variantSku")
    public String getVariantSku() {
        if (variant != null) {
            if (variant.getSku() != null && !variant.getSku().isEmpty()) {
                return variant.getSku();
            }
            return "Variant #" + variant.getId();
        }
        return null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("variantSku")
    public void setVariantSku(String variantSku) {
        // Accept for deserialization
    }
}
