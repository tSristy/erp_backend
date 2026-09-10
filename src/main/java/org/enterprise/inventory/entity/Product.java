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
            this.prices.removeIf(existing -> prices.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductPrice item : prices) {
                if (item.getId() == null || this.prices.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.prices.add(item);
                } else {
                    ProductPrice existing = this.prices.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.prices.clear();
        }
    }

    public void setSuppliers(java.util.List<ProductSupplier> suppliers) {
        if (suppliers != null) {
            this.suppliers.removeIf(existing -> suppliers.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductSupplier item : suppliers) {
                if (item.getId() == null || this.suppliers.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.suppliers.add(item);
                } else {
                    ProductSupplier existing = this.suppliers.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.suppliers.clear();
        }
    }

    public void setTaxes(java.util.List<ProductTax> taxes) {
        if (taxes != null) {
            this.taxes.removeIf(existing -> taxes.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductTax item : taxes) {
                if (item.getId() == null || this.taxes.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.taxes.add(item);
                } else {
                    ProductTax existing = this.taxes.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.taxes.clear();
        }
    }

    public void setImages(java.util.List<ProductImage> images) {
        if (images != null) {
            this.images.removeIf(existing -> images.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductImage item : images) {
                if (item.getId() == null || this.images.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.images.add(item);
                } else {
                    ProductImage existing = this.images.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.images.clear();
        }
    }

    public void setAttributes(java.util.List<ProductAttributeValue> attributes) {
        if (attributes != null) {
            this.attributes.removeIf(existing -> attributes.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductAttributeValue item : attributes) {
                if (item.getId() == null || this.attributes.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.attributes.add(item);
                } else {
                    ProductAttributeValue existing = this.attributes.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.attributes.clear();
        }
    }

    public void setVariants(java.util.List<ProductVariant> variants) {
        if (variants != null) {
            this.variants.removeIf(existing -> variants.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductVariant item : variants) {
                if (item.getId() == null || this.variants.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.variants.add(item);
                } else {
                    ProductVariant existing = this.variants.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.variants.clear();
        }
    }

    public void setUomConversions(java.util.List<ProductUomConversion> uomConversions) {
        if (uomConversions != null) {
            this.uomConversions.removeIf(existing -> uomConversions.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductUomConversion item : uomConversions) {
                if (item.getId() == null || this.uomConversions.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setProduct(this);
                    this.uomConversions.add(item);
                } else {
                    ProductUomConversion existing = this.uomConversions.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "product");
                }
            }
        } else {
            this.uomConversions.clear();
        }
    }

    public void setBundles(java.util.List<ProductBundle> bundles) {
        if (bundles != null) {
            this.bundles.removeIf(existing -> bundles.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for (ProductBundle item : bundles) {
                if (item.getId() == null || this.bundles.stream().noneMatch(e -> e.getId().equals(item.getId()))) {
                    item.setParentProduct(this);
                    this.bundles.add(item);
                } else {
                    ProductBundle existing = this.bundles.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing != null) org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "parentProduct");
                }
            }
        } else {
            this.bundles.clear();
        }
    }
}
