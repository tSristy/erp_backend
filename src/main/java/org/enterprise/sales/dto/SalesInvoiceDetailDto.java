package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class SalesInvoiceDetailDto extends AuditableDto {
    private Long salesInvoiceId;
    private Long deliveryNoteDetailId;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantSku;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountTotal;
    private java.util.List<SalesInvoiceDetailDiscountDto> discounts;

    @com.fasterxml.jackson.annotation.JsonAlias({"variant_id", "variant"})
    public void setVariant(Object variant) {
        if (variant instanceof Number) {
            this.variantId = ((Number) variant).longValue();
        } else if (variant instanceof java.util.Map) {
            Object idVal = ((java.util.Map<?, ?>) variant).get("id");
            if (idVal instanceof Number) {
                this.variantId = ((Number) idVal).longValue();
            } else if (idVal != null) {
                try {
                    this.variantId = Long.parseLong(idVal.toString());
                } catch (NumberFormatException ignored) {}
            }
        } else if (variant != null) {
            try {
                this.variantId = Long.parseLong(variant.toString());
            } catch (NumberFormatException ignored) {}
        }
    }

    public Object getVariant() {
        if (this.variantId == null) {
            return null;
        }
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", this.variantId);
        if (this.variantSku != null) {
            map.put("sku", this.variantSku);
        }
        return map;
    }
}
