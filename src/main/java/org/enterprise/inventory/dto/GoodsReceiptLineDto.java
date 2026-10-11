package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GoodsReceiptLineDto {

    @com.fasterxml.jackson.annotation.JsonAlias({"product_id", "product"})
    private Long productId;

    @com.fasterxml.jackson.annotation.JsonAlias({"variant_id"})
    private Long variantId;

    @com.fasterxml.jackson.annotation.JsonSetter("variant")
    public void setVariant(Object variant) {
        if (variant instanceof Number) {
            this.variantId = ((Number) variant).longValue();
        } else if (variant instanceof String) {
            try {
                this.variantId = Long.valueOf((String) variant);
            } catch (NumberFormatException ignored) {}
        } else if (variant instanceof java.util.Map) {
            Object id = ((java.util.Map<?, ?>) variant).get("id");
            if (id instanceof Number) {
                this.variantId = ((Number) id).longValue();
            } else if (id instanceof String) {
                try {
                    this.variantId = Long.valueOf((String) id);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @com.fasterxml.jackson.annotation.JsonSetter("product")
    public void setProduct(Object product) {
        if (product instanceof Number) {
            this.productId = ((Number) product).longValue();
        } else if (product instanceof String) {
            try {
                this.productId = Long.valueOf((String) product);
            } catch (NumberFormatException ignored) {}
        } else if (product instanceof java.util.Map) {
            Object id = ((java.util.Map<?, ?>) product).get("id");
            if (id instanceof Number) {
                this.productId = ((Number) id).longValue();
            } else if (id instanceof String) {
                try {
                    this.productId = Long.valueOf((String) id);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private BigDecimal quantity;

    private BigDecimal unitCost;

    private String batchNo;

    private java.time.LocalDate manufactureDate;

    private java.time.LocalDate expiryDate;

    private java.util.List<String> serialNumbers;
}
