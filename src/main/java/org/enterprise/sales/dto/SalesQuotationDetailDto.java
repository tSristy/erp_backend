package org.enterprise.sales.dto;

import lombok.Data;
import java.math.BigDecimal;
import org.enterprise.common.dto.AuditableDto;

@Data
public class SalesQuotationDetailDto extends AuditableDto {
    private Long salesQuotationId;
    private Long productId;
    private String productName;
    private Long variantId;
    private String variantSku;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private BigDecimal discountTotal;
    @com.fasterxml.jackson.annotation.JsonProperty("vatRate")
    @com.fasterxml.jackson.annotation.JsonAlias({"taxRate", "vat_rate", "tax_rate"})
    private BigDecimal vatRate;

    @com.fasterxml.jackson.annotation.JsonProperty("isVatInclusive")
    @com.fasterxml.jackson.annotation.JsonAlias({"vatInclusive", "is_vat_inclusive", "vat_inclusive"})
    private Boolean isVatInclusive;

    @com.fasterxml.jackson.annotation.JsonProperty("vatTotal")
    @com.fasterxml.jackson.annotation.JsonAlias({"vatAmount", "taxTotal", "vat_total", "tax_total"})
    private BigDecimal vatTotal;

    private java.util.List<SalesQuotationDetailDiscountDto> discounts;

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
