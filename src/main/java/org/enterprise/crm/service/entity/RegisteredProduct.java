package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.ProductVariant;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "crm_registered_products")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class RegisteredProduct extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    private String productName;

    private String brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BusinessPartner customer;

    private String serialNumber;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    @Enumerated(EnumType.STRING)
    private RegisteredProductStatus status = RegisteredProductStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "crm_registered_product_attrs", joinColumns = @JoinColumn(name = "registered_product_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    public enum RegisteredProductStatus {
        ACTIVE, INACTIVE, REPLACED, RETIRED
    }

    @Transient
    private Long transientProductId;

    @Transient
    private Long transientCustomerId;

    @Transient
    private Long transientVariantId;

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

    @com.fasterxml.jackson.annotation.JsonProperty("customerId")
    public Long getCustomerId() {
        return customer != null ? customer.getId() : transientCustomerId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("customerId")
    public void setCustomerId(Long customerId) {
        this.transientCustomerId = customerId;
        if (customerId != null) {
            if (this.customer == null) {
                this.customer = new BusinessPartner();
            }
            this.customer.setId(customerId);
        }
    }

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
    @com.fasterxml.jackson.annotation.JsonProperty("customerName")
    public String getCustomerName() {
        return customer != null ? customer.getName() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("customerName")
    public void setCustomerName(String customerName) {
        // Accept for deserialization
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