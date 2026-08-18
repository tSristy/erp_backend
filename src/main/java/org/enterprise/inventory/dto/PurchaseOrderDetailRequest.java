package org.enterprise.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderDetailRequest {

    @com.fasterxml.jackson.annotation.JsonAlias({"itemId", "item_id", "product_id"})
    private Long productId;

    private BigDecimal qty;

    private BigDecimal unitPrice;

    private List<PurchaseOrderDetailCostRequest> costs;

    @com.fasterxml.jackson.annotation.JsonSetter("product")
    public void setProduct(Object product) {
        if (product instanceof Number) {
            this.productId = ((Number) product).longValue();
        } else if (product instanceof java.util.Map) {
            Object id = ((java.util.Map<?, ?>) product).get("id");
            if (id instanceof Number) {
                this.productId = ((Number) id).longValue();
            } else if (id instanceof String) {
                this.productId = Long.valueOf((String) id);
            }
        }
    }
    
    @com.fasterxml.jackson.annotation.JsonSetter("item")
    public void setItem(Object item) {
        setProduct(item);
    }
}