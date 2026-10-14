package org.enterprise.crm.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.inventory.entity.BusinessPartner;
import org.enterprise.inventory.entity.Product;
import org.enterprise.inventory.entity.ProductVariant;

@Entity
@Table(name = "crm_service_requests")
@Getter
@Setter
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceRequest extends AuditableEntity {

    @Column(unique = true)
    private String requestNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_product_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private RegisteredProduct registeredProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BusinessPartner customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    private String productName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProductVariant variant;

    @Column(columnDefinition = "TEXT")
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    private ServiceRequestPriority priority = ServiceRequestPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    private ServiceRequestStatus status = ServiceRequestStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Enumerated(EnumType.STRING)
    private ServiceLocationType serviceLocationType = ServiceLocationType.IN_SHOP;

    @Column(columnDefinition = "TEXT")
    private String serviceAddress;

    public enum ServiceRequestPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum ServiceRequestStatus {
        OPEN, PENDING_ESTIMATE, WAITING_APPROVAL, APPROVED, IN_PROGRESS, RESOLVED, READY_FOR_DELIVERY, DELIVERED, CLOSED
    }

    public enum ServiceLocationType {
        IN_SHOP, ON_SITE, REMOTE
    }

    @Transient
    private Long transientRegisteredProductId;

    @Transient
    private Long transientCustomerId;

    @Transient
    private Long transientProductId;

    @Transient
    private Long transientVariantId;

    @com.fasterxml.jackson.annotation.JsonProperty("registeredProductId")
    public Long getRegisteredProductId() {
        return registeredProduct != null ? registeredProduct.getId() : transientRegisteredProductId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("registeredProductId")
    public void setRegisteredProductId(Long registeredProductId) {
        this.transientRegisteredProductId = registeredProductId;
        if (registeredProductId != null) {
            if (this.registeredProduct == null) {
                this.registeredProduct = new RegisteredProduct();
            }
            this.registeredProduct.setId(registeredProductId);
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
