package org.enterprise.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enterprise.common.entity.AuditableEntity;
import org.enterprise.finance.entity.Account;
import org.enterprise.inventory.enums.CostingMethod;
import org.enterprise.inventory.enums.ProductType;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"company_id", "sku"})
        }
)
@Getter
@Setter
public class Product extends AuditableEntity {

    @Column(nullable = false)
    private String sku;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private UnitOfMeasure baseUom;

    private Boolean isBatchManaged = false;

    private Boolean isSerialManaged = false;

    @Enumerated(EnumType.STRING)
    private CostingMethod costingMethod = CostingMethod.AVERAGE;

    private Boolean inventoryItem = true;

    private Boolean purchasable = true;

    private Boolean saleable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account inventoryAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account cogsAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account salesAccount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductPrice> prices = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductSupplier> suppliers = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductTax> taxes = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductImage> images = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductAttributeValue> attributes = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductVariant> variants = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("product")
    private java.util.List<ProductUomConversion> uomConversions = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"parentProduct"})
    private java.util.List<ProductBundle> bundles = new java.util.ArrayList<>();

    public void setPrices(java.util.List<ProductPrice> prices) {
        if (prices != null) {
            this.prices.clear();
            this.prices.addAll(prices);
            for (ProductPrice p : this.prices) {
                p.setProduct(this);
            }
        } else {
            this.prices.clear();
        }
    }

    public void setSuppliers(java.util.List<ProductSupplier> suppliers) {
        if (suppliers != null) {
            this.suppliers.clear();
            this.suppliers.addAll(suppliers);
            for (ProductSupplier p : this.suppliers) {
                p.setProduct(this);
            }
        } else {
            this.suppliers.clear();
        }
    }

    public void setTaxes(java.util.List<ProductTax> taxes) {
        if (taxes != null) {
            this.taxes.clear();
            this.taxes.addAll(taxes);
            for (ProductTax p : this.taxes) {
                p.setProduct(this);
            }
        } else {
            this.taxes.clear();
        }
    }

    public void setImages(java.util.List<ProductImage> images) {
        if (images != null) {
            this.images.clear();
            this.images.addAll(images);
            for (ProductImage p : this.images) {
                p.setProduct(this);
            }
        } else {
            this.images.clear();
        }
    }

    public void setAttributes(java.util.List<ProductAttributeValue> attributes) {
        if (attributes != null) {
            this.attributes.clear();
            this.attributes.addAll(attributes);
            for (ProductAttributeValue p : this.attributes) {
                p.setProduct(this);
            }
        } else {
            this.attributes.clear();
        }
    }

    public void setVariants(java.util.List<ProductVariant> variants) {
        if (variants != null) {
            this.variants.clear();
            this.variants.addAll(variants);
            for (ProductVariant p : this.variants) {
                p.setProduct(this);
            }
        } else {
            this.variants.clear();
        }
    }

    public void setUomConversions(java.util.List<ProductUomConversion> uomConversions) {
        if (uomConversions != null) {
            this.uomConversions.clear();
            this.uomConversions.addAll(uomConversions);
            for (ProductUomConversion p : this.uomConversions) {
                p.setProduct(this);
            }
        } else {
            this.uomConversions.clear();
        }
    }

    public void setBundles(java.util.List<ProductBundle> bundles) {
        if (bundles != null) {
            this.bundles.clear();
            this.bundles.addAll(bundles);
            for (ProductBundle p : this.bundles) {
                p.setParentProduct(this);
            }
        } else {
            this.bundles.clear();
        }
    }
}
